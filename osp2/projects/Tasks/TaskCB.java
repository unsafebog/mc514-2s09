package osp.Tasks;

import java.util.Vector;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Ports.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.*;
import osp.Hardware.*;


public class TaskCB extends IflTaskCB
{

	//Structure that handles objects
	static ThreadCB threads[];
	static PortCB ports[];
	static OpenFile files[];
	
	static int threadCounter;
	static int portCounter;
	static int fileCounter;
	
	static float time;
	static int priority;
	
	public TaskCB(int p)
	{
		super();
		priority = p;
	}

	public static void init(TaskCB task)
	{
		task.threadCounter = 0;
		task.portCounter = 0;
		task.fileCounter = 0;
	}

	static public TaskCB do_create()
	{
		TaskCB newTask = new TaskCB(0); // Create task object with 0 priority
		
		PageTable newPageTable = new PageTable(newTask); //Create page table for the task.
		newTask.setPageTable(newPageTable); //Set page table
		
		time = HClock.get(); // Get task time.
		
		newTask.setStatus(TaskLive); //Set task status
		
		newTask.setPriority(priority);// set task priority
		
		//Swap File
		
		threads[0] = ThreadCB.create(newTask); //Create the first thread for the task
		threadCounter++;
		
		return newTask;
	}

	public void do_kill()
	{
		
		
		
	}
	
	public int do_getThreadCount(TaskCB task)
	{
		return task.threadCounter;
	}

	public int do_addThread(ThreadCB thread)
	{
		
		return 0;
	}

	public int do_removeThread(ThreadCB thread)
	{
		return 1;//Success
	}
	
	public void do_killAllThreads(TaskCB task)
	{
		
			
	}
	
	

	public int do_getPortCount(TaskCB task)
	{
		return task.portCounter;
	}

	public int do_addPort(PortCB newPort)
	{
		return 0;
	}

	public int do_removePort(PortCB oldPort)
	{
		return 0;
	}

	public void do_addFile(OpenFile file)
	{
	}

	public int do_removeFile(OpenFile file)
	{
		return 0;
	}

	public static void atError()
	{
	}

	public static void atWarning()
	{
	}
	

}

