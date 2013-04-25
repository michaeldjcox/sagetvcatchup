package uk.co.mdjcox.sagetv.onlinevideo;

import com.google.inject.assistedinject.Assisted;

/**
 * Created with IntelliJ IDEA.
 */
public interface PublisherFactory {
  Publisher createPublisher(@Assisted("qualifier") String qualifier, @Assisted("STV") String stvDirectory);

}
