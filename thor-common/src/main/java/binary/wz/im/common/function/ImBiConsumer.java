package binary.wz.im.common.function;

/**
 * @author binarywz
 * @date 2022/4/25 23:29
 * @description:
 */
@FunctionalInterface
public interface ImBiConsumer<T, U> {
    /**
     * perform this operation on the given arguments
     * @param t the first argument
     * @param u the second argument
     * @throws Exception
     */
    void accept(T t, U u) throws Exception;
}
