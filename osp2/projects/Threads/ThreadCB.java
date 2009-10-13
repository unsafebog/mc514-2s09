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
	//Static variables because the list is shared between all threads
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
		error = task.addThread(thread);
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
		readyQueue.insert(thread);
		
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
		//Auxiliars
		int DeviceSize;
		Device device;
		TaskCB task = this.getTask();
		
		//get status from thread to verify cases
		int status = this.getStatus();

		switch(status)
		{
			case ThreadReady://if ready remove from ready queue
				readyQueue.remove(this); 
				break;
			
			case ThreadRunning: //if running remove cpu control then remove
				ThreadCB.dispatch();
				readyQueue.remove(this);
				break;
			
			case ThreadWaiting:
				DeviceSize = Device.getTableSize();
				for(int i = 0; i < DeviceSize; i++)
				{
					device = Device.get(i);
					device.cancelPendingIO(this);
				}
				break;
			default:
				DeviceSize = Device.getTableSize();
				for(int i = 0; i < DeviceSize; i++)
				{
					device = Device.get(i);
					device.cancelPendingIO(this);
				}
				break;
		} 
		
		//give up thread resources
		ResourceCB.giveupResources(this);
		//Set status
		this.setStatus(ThreadKill);

		//Remove the dead thread
		task.removeThread(this);
		//Dipatch new thread
		ThreadCB.dispatch();
		if(task.getThreadCount() == 0)
		{
			//if has no more threads, kill task
			task.kill();
		}

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
		if(status == ThreadRunning)
		{
			this.setStatus(ThreadWaiting);
			waitingQueue.insert(this);
			event.addThread(this);
		}
		else if( status != ThreadRunning)
		{
			event.removeThread(this);
			//set new status
			status += 1;
			this.setStatus(status);
			event.addThread(this);
		}
		else
			ThreadCB.atError();
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
		int status = this.getStatus();
		
		//Status waiting > ready
		if(status == ThreadWaiting)
		{
			waitingQueue.remove(this);
			readyQueue.insert(this);
			this.setStatus(ThreadReady);
		}
		else if(status != ThreadWaiting)
		{
			status -= 1;
			this.setStatus(status);
		}
		else
			return;
		
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
		PageTable pagetable;
		TaskCB task;
		ThreadCB thread, threaddispatched;
		if(readyQueue.isEmpty())
			return SUCCESS;
		else if(!readyQueue.isEmpty())
		{
			threaddispatched = ThreadCB.scheduler();
			
			//preempt current running thread
			//Get current running thread
			pagetable = MMU.getPTBR(); //step 1: get thread pagetable
			if(pagetable == null && threaddispatched != null)
			{
				task = threaddispatched.getTask();//get the task that t belongs to
				
				pagetable = task.getPageTable();//get task pagetable
				
				MMU.setPTBR(pagetable);//Set pagetable register
				
				threaddispatched.setStatus(ThreadRunning); // set t status
			
				task.setCurrentThread(threaddispatched); //set new current thread
				
				return SUCCESS;
			}
			
			//preempting
			task = pagetable.getTask(); //step 2:get running task
			thread = task.getCurrentThread(); // steap 3:get current running thread
				
			// Set status
			if(thread.getTimeOnCPU() > 10.f)
			{
				readyQueue.insert(thread);
				thread.setStatus(ThreadReady);
			}
			else
			{
				thread.setStatus(ThreadWaiting);
				waitingQueue.insert(thread);
			}
			//dispatch
			task = threaddispatched.getTask();//get the task that t belongs to
			
			pagetable = task.getPageTable();//get task pagetable
			
			MMU.setPTBR(pagetable);//Set pagetable register
			
			threaddispatched.setStatus(ThreadRunning); // set t status
		
			task.setCurrentThread(threaddispatched); //set new current thread
			
			
			
/* ------------------ CONTEXT SWITCHING -----------------------------*/
			return SUCCESS;
		}
		return FAILURE;
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
	
	public static ThreadCB scheduler()
	{
		ThreadCB thread;
		if(!readyQueue.isEmpty())
		{
			thread = (ThreadCB) readyQueue.removeHead();
			if(thread.getStatus() == ThreadReady)
					return thread;
		}
		else
			return null;
		return null;
	}
	
	
	
	
	
}

