package osp.Resources;

import java.util.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Memory.*;


public class ResourceCB extends IflResourceCB
{
	RRB rrb;

	public ResourceCB(int qty)
	{
		super(qty);
	}

	public static void init()
	{
	}

	public RRB  do_acquire(int quantity) 
	{
		TaskCB task = MMU.getPTBR().getTask();
		rrb = new RRB(quantity);
		switch(RRB.getReasourceMethod())
		{
			case Avoidance:
				if(this.bankers() == 1)
				{
					rrb.grant();
					return rrb;
				}
				else
				{
					rrb.setStatus(Suspended);
					task.getCurrentThread().suspend();
					return rrb;
				}
				break;
				
			case Detection:
				if(quantity <= this.getAvailable())
				{
					rrb.grant();
					return rrb;
				}
				else if( quantity + this.getAllocated(task.getCurrentThread()) > this.getTotal() )
					return null;
				else
				{
					rrb.setStatus(Suspended);
					task.getCurrentThread().suspend();
					return rrb;
				}
				break;
		}
		
		return null;
	}

	public static Vector do_deadlockDetection()
	{
	// your code goes here

	}

    /**
       When a thread was killed, this is called to release all
       the resources owned by that thread.

       @param thread -- the thread in question

       @OSPProject Resources
    */
    public static void do_giveupResources(ThreadCB thread)
    {
        // your code goes here

    }

	public void do_release(int quantity)
	{
		ThreadCB thread = MMU.getPTBR().getTask().getCurrentThread();
		this.setAllocated(thread, this.getAllocated(thread)-quantity);
		this.setAvailable(this.getAvailable()+quantity);
	}

	public static void atError()
	{
	// your code goes here

	}

	public static void atWarning()
	{
	// your code goes here

	}
	
	
	///Implementar o algoritmo do bankeiro
	public int bankers()
	{
		if(1)
			return 1;
		return 0;
	}
}

