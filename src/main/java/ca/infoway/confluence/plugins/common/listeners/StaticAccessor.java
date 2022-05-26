package ca.infoway.confluence.plugins.common.listeners;

import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.sal.api.transaction.TransactionTemplate;

// NOTE: originally used in SpaceListener.java, but is no longer used
public class StaticAccessor {
	 
    private static SpaceManager spaceManager;
 
    private static TransactionTemplate transactionTemplate;
  
	public StaticAccessor(SpaceManager spaceManager, final TransactionTemplate transactionTemplate) {
        StaticAccessor.spaceManager = spaceManager;
        StaticAccessor.transactionTemplate = transactionTemplate;
    }

    public static SpaceManager getSpaceManager() {
        return spaceManager;
    }

    public static TransactionTemplate getTransactionTemplate() {
  		return transactionTemplate;
  	}

}