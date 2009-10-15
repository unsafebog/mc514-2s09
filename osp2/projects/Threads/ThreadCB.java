package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads
*/
public class ThreadCB extends IflThreadCB 
{
	//Create ready queue list
	static GenericList readyQueue;

	
	public ThreadCB()
	{
		super();
		readyQueue = new GenericList();
	}

	/**
	This method will be called once at the beginning of the
	simulation. The student can set up static variables here.

	@OSPProject Threads
	*/
	public static void init()
	{
	}

	/** 
	Sets up a new thread and adds it to the given task. 
	The method must set the ready status 
	and attempt to add thread to task. If the latter fails 
	because there are already too many threads in this task, 
	so does this method, otherwise, the thread is appended 
	to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

	@OSPProject Threads
	*/
	public static ThreadCB do_create(TaskCB task)
	{
			//if task thread limit is not exceeded
		if(task.getThreadCount() >= MaxThreadsPerTask)
		{
			ThreadCB.dispatch();
			return null;
		}
			//new thread object
		ThreadCB newThread = new ThreadCB();
			//add thread to task
		if(task.addThread(newThread) == FAILURE)
			return null;
			//set thread task
		newThread.setTask(task);
		
		//Set Priority --------------------------------------
			//set new status
		newThread.setStatus(ThreadReady);
			//put in queue
		readyQueue.append(newThread);
		
		ThreadCB.dispatch();
		
		return newThread;
		
	}

	/** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.

	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.

	@OSPProject Threads
	*/
	public void do_kill()
	{
		Device device;
		TaskCB task = this.getTask();
		
		switch(this.getStatus())
		{
				//if ready remove from ready queue
			case ThreadReady:
				readyQueue.remove(this); 
				break;
				//if running remove cpu control then remove
			case ThreadRunning: 
				ThreadCB.dispatch();
				readyQueue.remove(this);
				break;
				//if ThreadWaiting+n cancel pending IO
			default:
				for(int i = 0; i < Device.getTableSize(); i++)
					Device.get(i).cancelPendingIO(this);
				break;
		} 
			//give up thread resources
		ResourceCB.giveupResources(this);
			//Set status
		this.setStatus(ThreadKill);
			//Remove the dead thread
		task.removeThread(this);
			//if has no more threads, kill task
		if(task.getThreadCount() == 0)
			task.kill();
			//Dipatch new thread
		ThreadCB.dispatch();
	}

	/** Suspends the thread that is currenly on the processor on the 
	specified event. 

	Note that the thread being suspended doesn't need to be
	running. It can also be waiting for completion of a pagefault
	and be suspended on the IORB that is bringing the page in.

	Thread's status must be changed to ThreadWaiting or higher,
	the processor set to idle, the thread must be in the right
	waiting queue, and dispatch() must be called to give CPU
	control to some other thread.

	@param event - event on which to suspend this thread.

	@OSPProject Threads
	*/
	public void do_suspend(Event event)
	{
		switch(this.getStatus())
		{
			case ThreadRunning:
				MMU.getPTBR().getTask().setCurrentThread(null);
				MMU.setPTBR(null);
				this.setStatus(ThreadWaiting);
				event.addThread(this);
				break;
			case ThreadReady:
				readyQueue.remove(this);
				this.setStatus(ThreadWaiting);
				event.addThread(this);
				return;
			case ThreadWaiting:
				event.removeThread(this);
				this.setStatus(this.getStatus()+1);
				event.addThread(this);
				break;
			default:
				event.removeThread(this);
				this.setStatus(this.getStatus()+1);
				event.addThread(this);
				break;
		}
		//dispatch new thread
		ThreadCB.dispatch();
		
	}

	/** Resumes the thread.

	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.

	@OSPProject Threads
	*/
	public void do_resume()
	{
			//Analog to Figure 2.1 from OSP book
		switch(this.getStatus())
		{
			case ThreadRunning:
			case ThreadReady:
				return;
			case ThreadWaiting:
				this.setStatus(ThreadReady);
				break;
				//ThreadWaiting+n case
			default:
				this.setStatus(this.getStatus()-1);
				break;
		}
			//Append thread ready on queue
		if(this.getStatus() == ThreadReady)
			readyQueue.append(this);
			
		ThreadCB.dispatch();
	}

	/** 
	Selects a thread from the run queue and dispatches it. 

	If there is just one theread ready to run, reschedule the thread 
	currently on the processor.

	In addition to setting the correct thread status it must
	update the PTBR.

	@return SUCCESS or FAILURE

	@OSPProject Threads
	*/
	public static int do_dispatch()
	{
		ThreadCB runningThread, readyThread;
		
			//If no running thread
		if(MMU.getPTBR() == null)
		{
			readyThread = (ThreadCB) readyQueue.removeHead();
			if(readyThread == null)
				return FAILURE;
			readyThread.setStatus(ThreadRunning);
			MMU.setPTBR(readyThread.getTask().getPageTable());
			readyThread.getTask().setCurrentThread(readyThread);
			return SUCCESS;
		}	
			//Get current running thread
		runningThread = MMU.getPTBR().getTask().getCurrentThread();	
			//if current running thread didn't spend quantum time, keep current running thread
		if(runningThread.getTimeOnCPU() < 0.001)
			return SUCCESS;
			
			//else, preempt running thread
		runningThread.setStatus(ThreadReady);
		readyQueue.append(runningThread);
		MMU.setPTBR(null);
		runningThread.getTask().setCurrentThread(null);
			//dispatching
			
		readyThread = (ThreadCB) readyQueue.removeHead();
			//if thre is no ready to run thread, put last running thread to run
		while(readyThread == null)
			readyThread = (ThreadCB) readyQueue.removeHead();
// 		if(readyThread == null)
// 		{
// 			readyQueue.remove(runningThread);
// 			MMU.setPTBR(runningThread.getTask().getPageTable());
// 			runningThread.getTask().setCurrentThread(runningThread);
// 			return SUCCESS;
// 		}
		readyThread.setStatus(ThreadRunning);
		MMU.setPTBR(readyThread.getTask().getPageTable());
		readyThread.getTask().setCurrentThread(readyThread);
		return SUCCESS;
	}

	/**
	Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures in
	their state just after the error happened.  The body can be
	left empty, if this feature is not used.

	@OSPProject Threads
	*/
	public static void atError()
	{
		System.out.printf("Error Here\n\n\n");

	}

	/** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.

	@OSPProject Threads
	*/
	public static void atWarning()
	{
	// your code goes here

	}

	
	
	
	
	
}

