package osp.Resources;

import java.util.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Memory.*;

/**
    Class ResourceCB is the core of the resource management module.
    Students implement all the do_* methods.
    @OSPProject Resources
*/

public class ResourceCB extends IflResourceCB
{
	public ResourceCB(int qty)
	{
		super(qty);
	}
	public static void init()
	{
		// your code goes here

	}

	public RRB do_acquire(int quantity) 
	{
		//get thread wich request the resource
		ThreadCB thread = MMU.getPTBR().getTask().getCurrentThread();
		RRB rrb = new RRB(thread, this, quantity);
		switch(ResourceCB.getDeadlockMethod())
		{
			case Detection:
				if(quantity > rrb.resource.getTotal())
					return null;
				else if(quantity > rrb.resource.getAvailable())
				{
					rrb.setStatus(Suspended);
					rrb.thread.suspend(rrb);
					return rrb;
				}
				else
				{
					rrb.grant();
					return rrb;
				}	
				break;
			case Avoidance:
				int bankers = 0;
				if(this.getMaxClaim(rrb.thread)-quantity <= this.getAvailable())
				{
					rrb.grant();
					return rrb;
				}
				else
				{
					rrb.setStatus(Suspended);
					rrb.thread.suspend(rrb);
					return null;
				}
				break;
		}
	}

    /**
       Performs deadlock detection.
       @return A vector of ThreadCB objects found to be in a deadlock.

       @OSPProject Resources
    */
	public static Vector do_deadlockDetection()
	{
		

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

    /**
        Release a previously acquired resource.

	@param quantity

        @OSPProject Resources
    */
	public void do_release(int quantity)
	{
		ThreadCB thread = MMU.getPTBR().getTask().getCurrentThread();
		int qty = this.getAllocated(thread);
		this.setAllocated(thread, 0);
		this.setAvailable(qty+this.getAvailable());
	}

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
	
	@OSPProject Resources
    */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
	@OSPProject Resources
    */
    public static void atWarning()
    {
        // your code goes here

    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
