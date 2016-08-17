
package org.onosproject.oxp.types;

/**
 * Created by cr on 16-4-7.
 */
public interface OXPValueType<T extends OXPValueType<T>> extends Comparable<T>, PrimitiveSinkable {
    public int getLength();

    public T applyMask(T mask);
}
