package osp.Resources;

import java.util.*;

import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;

public class ResourceTable extends IflResourceTable
{
	ResourceCB rTable[];
	int size;
	
	public ResourceTable()  
	{
		super();
		size = ResourceTable.getSize();
		rTable = new ResourceCB[size];
		for(int i=0; i < size ; i++)
			rTable[i] = getResourceCB(i);
	}
}
