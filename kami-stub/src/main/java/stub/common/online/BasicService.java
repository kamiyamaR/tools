package stub.common.online;

/**
 * 
 * @author kamiyama ryohei
 *
 * @param <P>
 * @param <R>
 */
public interface BasicService<P, R> {

    /**
     * 
     * @param param
     * @return
     */
    public R execute(P param);

}
