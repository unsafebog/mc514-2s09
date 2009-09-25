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

	static private GenericList threads;
	static private GenericList ports;
	static private GenericList files;
	static private TaskCB newTask;
	static private PageTable newPageTable;
	static private String SwapPathName;
	
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
		newTask = new TaskCB();
		
		//Create page table
		newPageTable = new PageTable(newTask);
		//Set Ttask's pagetable
		newTask.setPageTable(newPageTable);
		
		//Set task' status
		newTask.setStatus(TaskLive);
		
		//Set creation time
		newTask.setCreationTime(HClock.get());
		
		//Creates swap file
		SwapPathName = SwapDeviceMountPoint;
		SwapPathName.concat(Integer.toString(newTask.getID()));
		int createdSwapFile = FileSys.create(SwapPathName, MMU.getVirtualAddressBits());
		//  AQUI FAZER O ERROR ****************************************************************
		
		//Open swap file
		OpenFile swap = OpenFile.open(SwapDeviceMountPoint, newTask);
		newTask.setSwapFile(swap);
		//  AQUI FAZER O ERROR ****************************************************************
		
		threads.insert(ThreadCB.create(newTask));
		
		return newTask;
	}

	
	public void do_kill()
	{
		while(threads.length() != 0)
			newTask.do_removeThread((ThreadCB) threads.getHead());
		while(ports.length() != 0)
			newTask.do_removePort((PortCB) ports.getHead());
		
		//Setar status da task
		newTask.setStatus(TaskTerm);
		
		newPageTable.deallocateMemory();
		
		
		while(files.length() != 0)
			newTask.do_removeFile((OpenFile) files.getHead());
		
		FileSys.delete(SwapPathName);
	}

	
	public int do_getThreadCount()
	{
		return threads.length();

	}

	
	public int do_addThread(ThreadCB thread)
	{
		if(newTask.do_getThreadCount() < ThreadCB.MaxThreadsPerTask())
		{
			threads.insert(thread);
			return 1;
		}
		else
			return 0;
	}

	
	public int do_removeThread(ThreadCB thread)
	{
		
		if(threads.remove(thread) != null)
			return 1;
		else
			return 0;

	}

	public int do_getPortCount()
	{
		
		return ports.length();
	}


	public int do_addPort(PortCB newPort)
	{
		if(ports.length() < PortCB.MaxPortsPerTask())
		{
			ports.insert(newPort);
			return 1;
		}
		else
			return 0;

	}

	public int do_removePort(PortCB oldPort)
	{
		if(ports.remove(oldPort) != null)
			return 1;
		else
			return 0;
	}

	public void do_addFile(OpenFile file)
	{


	}

	public int do_removeFile(OpenFile file)
	{
		if(files.remove(file) != null)
		{
			file.close();
			return 1;
		}
		else
			return 0;
	}
	public static void atError()
	{
		System.out.println("oi");

	}

	public static void atWarning()
	{


	}

	
}
