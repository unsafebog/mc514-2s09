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

	private GenericList threads;
	private GenericList ports;
	private GenericList files;
	
	private ThreadCB firstThread;
	private PageTable pageTable;

	private String SwapPathName;
	
	public TaskCB()
	{
		super();
	}

	
	public static void init()
	{

	}

	static public TaskCB do_create()
	{
		OpenFile swapFile;
		int createdSwapFile;
		
		//create task object 
		TaskCB newTask = new TaskCB();
		
		//Create and set page table
		PageTable newPageTable = new PageTable(newTask);
		newTask.setPageTable(newPageTable);
		newTask.pageTable = newPageTable;
		
		//Create appropriate structures for threads, ports and files
		newTask.threads = new GenericList();
		newTask.ports = new GenericList();
		newTask.files = new GenericList();
		
		//Set creation time
		newTask.setCreationTime(HClock.get());
		
		//Set task status
		newTask.setStatus(TaskLive);
		
		//Set task priority to 1, random value
		newTask.setPriority(1);
		
		//Create string with complete path name for swap file
		newTask.SwapPathName = SwapDeviceMountPoint + Integer.toString(newTask.getID());
		
		//Create swap file, condition for failure > return error
		createdSwapFile = FileSys.create(newTask.SwapPathName, (int)Math.pow(2, MMU.getVirtualAddressBits()));
		if( createdSwapFile == FAILURE )
			TaskCB.atError();
		
		//Handles de first thread for task
		newTask.firstThread =  ThreadCB.create(newTask);
		
		//Set swap file for the current task
		swapFile = OpenFile.open(newTask.SwapPathName, newTask);
		if(swapFile == null)
		{
			newTask.atError();
			newTask.firstThread.dispatch(); //Dispatch thread if open file fail
			return null;
		}
		
		//Set the swap file
		newTask.setSwapFile(swapFile);
		
		return newTask;
	}

	public void do_kill()
	{
		ThreadCB auxThread;
		PortCB auxPort;
		OpenFile auxFile;
		
		//Iteration for thread kill
		while(threads.length() >= 0)
		{
			auxThread = (ThreadCB) threads.removeHead();
				auxThread.kill();
		}
// 		this.firstThread.kill();
		
		//Iteration for port destroy
// 		while(this.ports.isEmpty() == false)
// 		{
// 			
// 			auxPort = (PortCB) this.ports.removeHead();
// 				auxPort.destroy();
// 		}
		
		//Set new task status
		this.setStatus(TaskTerm);
		
		//Deallocate memory
		this.pageTable.deallocateMemory();
		
		//Delete swap file
		FileSys.delete(this.SwapPathName);

		//Iteration for file close
		while(this.files.length() > 0)
		{
			auxFile = (OpenFile) this.files.removeHead();
			if(auxFile != null)
				auxFile.close();
		}
	}

	
	public int do_getThreadCount()
	{
		return threads.length();
	}

	
	public int do_addThread(ThreadCB thread)
	{
		if(threads.length() < ThreadCB.MaxThreadsPerTask) //Verify the thread limit is not excceeded
		{
			threads.append(thread);//insert new thread
			return SUCCESS;
		}
		else
		{
			this.atError();
			return FAILURE;
		}
	}

	public int do_removeThread(ThreadCB thread)
	{
		//remove thread from the list
		ThreadCB remove = (ThreadCB) threads.remove(thread);
		remove.dispatch();
		if(remove != null)
			return SUCCESS;
		else
		{
			this.atError();
			return FAILURE;
		}
	}

	public int do_getPortCount()
	{
		return ports.length();
	}


	public int do_addPort(PortCB newPort)
	{
		if(ports.length() < PortCB.MaxPortsPerTask)
		{
			ports.append(newPort);
			return SUCCESS;
		}
		else
		{
			this.atError();
			return FAILURE;
		}

	}

	public int do_removePort(PortCB oldPort)
	{
		if(ports.remove(oldPort) != null)
			return SUCCESS;
		else
		{
			this.atError();
			return FAILURE;
		}
	}

	public void do_addFile(OpenFile file)
	{
		files.append(file);
	}

	public int do_removeFile(OpenFile file)
	{
		if(files.remove(file) != null)
			return SUCCESS;
		else
		{
			this.atError();
			return FAILURE;
		}
	}
	public static void atError()
	{
		System.out.println("\n\n Error during operation.\n\n");
	}

	public static void atWarning()
	{
		System.out.println("\n\n Warning.\n");
	}

	
}
