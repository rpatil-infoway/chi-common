package ca.infoway.confluence.plugins.common.listeners;

import com.atlassian.event.api.EventPublisher;
import org.springframework.beans.factory.DisposableBean;

public class ScrollVersionsPublishEventListener implements DisposableBean
{
    protected final EventPublisher eventPublisher;

    public ScrollVersionsPublishEventListener(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        eventPublisher.register(this);
    }

    /**
     * Unregister the listener if the plugin is uninstalled or disabled.
     */
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    @com.atlassian.event.api.EventListener
    public void onVersionPublishEvent(Object event) {

        String eventName = event.getClass().getCanonicalName(); // will work for other people's events, not just Atlassian's

        // Spit out all events - just to prove the point
        System.out.println(" ++ an event happened: " + eventName);

        if (eventName.equals("com.k15t.scroll.platform.event.space.VersionPublishEvent")) {
            System.out.println(" ++++ Found the right event ");

            // Do your stuff here...
        }
    }
}