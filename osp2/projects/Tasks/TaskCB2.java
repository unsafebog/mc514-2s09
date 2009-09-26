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
	private TaskCB Task;
	private PageTable pageTable;
	

	
	private String SwapPathName;
	
	public TaskCB()
	{
		super();
		threads = new GenericList();
		ports = new GenericList();
		files = new GenericList();
	}

	
	public static void init()
	{

	}

	static public TaskCB do_create()
	{
		//create task object 
		TaskCB newTask = new TaskCB();
		
		//Create page table
		PageTable newPageTable = new PageTable(newTask);
		//Set Ttask's pagetable
		newTask.setPageTable(newPageTable);
		
		//Set task' status
		newTask.setStatus(TaskLive);
		
		//Set creation time
		newTask.setCreationTime(HClock.get());
		
		//Creates swap file
		String SwapPath = SwapDeviceMountPoint + Integer.toString(newTask.getID());
	
		int createdSwapFile = FileSys.create(SwapPath,(int) Math.pow(2, MMU.getVirtualAddressBits()));
	
		if( createdSwapFile == FAILURE )
			TaskCB.atError();
		
		OpenFile swapFile = OpenFile.open(SwapPath, newTask);
		newTask.setSwapFile(swapFile);

		ThreadCB fThread =  ThreadCB.create(newTask);
	
		return newTask;
	}

	
	public void do_kill()
	{
		
		ThreadCB auxThread;
		while(threads.isEmpty() != true)
		{
			auxThread = (ThreadCB) threads.removeHead();
			if(auxThread != null)
			{
				System.out.printf("\n\n\n%d",threads.length());
				auxThread.kill();
			}
		}
		
		
		PortCB auxPort = (PortCB) ports.removeHead();
		while(auxPort != null)
			auxPort = (PortCB) ports.removeHead();
		
		
		OpenFile auxFile = (OpenFile) files.removeHead();
		while(auxFile != null)
			auxFile = (OpenFile) files.removeHead();
		
		//Setar status da task
		Task.setStatus(TaskTerm);
		
		pageTable.deallocateMemory();
		
		
		FileSys.delete(SwapPathName);
	}

	
	public int do_getThreadCount()
	{
		return threads.length();

	}

	
	public int do_addThread(ThreadCB thread)
	{
		
		if(threads.length() < ThreadCB.MaxThreadsPerTask)
		{
			threads.append(thread);
			return SUCCESS;
		}
		else
			return FAILURE;
	}

	
	public int do_removeThread(ThreadCB thread)
	{
		ThreadCB remove = (ThreadCB) threads.remove(thread);
		if( remove != null)
			return SUCCESS;
		else
			return FAILURE;

	}

	public int do_getPortCount()
	{
		
		return ports.length();
	}


	public int do_addPort(PortCB newPort)
	{
		if(ports.length() < PortCB.MaxPortsPerTask)
		{
			ports.insert(newPort);
			return SUCCESS;
		}
		else
			return FAILURE;

	}

	public int do_removePort(PortCB oldPort)
	{
		if(ports.remove(oldPort) != null)
			return SUCCESS;
		else
			return FAILURE;
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
			return FAILURE;
	}
	public static void atError()
	{
		

	}

	public static void atWarning()
	{


	}

	
}
