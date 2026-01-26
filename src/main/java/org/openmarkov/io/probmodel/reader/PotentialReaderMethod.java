package org.openmarkov.io.probmodel.reader;

import org.openmarkov.core.model.network.potential.Potential;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD})
public @interface PotentialReaderMethod {
    Class<? extends Potential>[] value();
}
