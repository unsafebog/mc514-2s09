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
	static GenericList readyQueue;
	static GenericList waitingQueue;
	static ThreadCB running;

	
	public ThreadCB()
	{
		super();
		readyQueue = new GenericList();
		waitingQueue = new GenericList();
		running = null;
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
	
		int error;
		//Create thread object
		ThreadCB thread = new ThreadCB();
		
		//Attach thread to its task
		error =task.addThread(thread);
		if(error == FAILURE)
		{
			ThreadCB.atError();
			return null;
			}
		
		//Verify thread count
		if(task.getThreadCount() < MaxThreadsPerTask)
			thread.setTask(task);
		else
		{
			ThreadCB.atError();
			return null;
		}
		//Set priority -----------------------------------------
		
		//Set thread status and put on ready queue
		thread.setStatus(ThreadReady);
		thread.readyQueue.append(thread);
		
		//Thread dispatch
		ThreadCB.dispatch();
		
		return thread;
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
		//get status from thread to verify cases
		int status = this.getStatus();
		Device device;
		this.setStatus(ThreadKill);
		
		TaskCB task = this.getTask();
		
		switch(status)
		{
			case ThreadReady://if ready remove from ready queue
				this.readyQueue.remove(this); 
				break;
			
			case ThreadRunning: //if running remove cpu control
				ThreadCB.dispatch();
				break;
			
			case ThreadWaiting:
				for(int i = 0; i < Device.getTableSize(); i++)
				{
					device = Device.get(i);
					device.cancelPendingIO(this);
				}
				break;
		}
		
		//give up thread resources
		ResourceCB.giveupResources(this);
		
		if(task.getThreadCount() != 0)
		{
			//remove from thread ready queue
			this.readyQueue.removeHead();
			//set as running thread
			this.setStatus(ThreadRunning);
			this.running = this;
		}
		else
		{
			//if has no more threads, kill task
			task.kill();
		}

		this.readyQueue.remove(this);

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
		int status = this.getStatus();
		//case = thread already waiting
		if( status == ThreadWaiting )
		{
			//remove from queue
			this.waitingQueue.remove(this);
			//set new status
			this.setStatus(ThreadWaiting+1);
			//insert in the queue
			this.waitingQueue.append(this);
		}
		//case = thread running
		if( status == ThreadReady )
		{
			//lose control from CPU
			this.dispatch();
			//Set new status
			this.setStatus(ThreadWaiting);
			//put in queue
			this.waitingQueue.append(this);
		}
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
		int status = this.getStatus();
		//Status waiting > ready
		if(status == ThreadWaiting)
		{
			this.waitingQueue.remove(this);
			this.readyQueue.append(this);
			this.setStatus(ThreadReady);
		}
		if(status != ThreadWaiting)
			ThreadCB.atError();
		else
			this.setStatus(ThreadWaiting-1);
		
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
		//FCFS scheduling
		ThreadCB thdispatch = (ThreadCB) readyQueue.removeHead();
		
		//Put running thread in ready queue, change status
		readyQueue.append(running);
		running.setStatus(ThreadReady);
		
		//put schedule thread in running status
		running = thdispatch;
		running.setStatus(ThreadRunning);
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

