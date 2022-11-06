package stub.common.online;

import stub.common.aop.annotation.OnlineProcessEntry;

/**
 * 
 * @author kamiyama ryohei
 *
 * @param <P>
 * @param <R>
 */
public abstract class AbstractService<P, R> implements BasicService<P, R> {

    /**
     * 
     * @param param
     * @return
     */
    public abstract R process(P param);

    @Override
    @OnlineProcessEntry
    public R execute(P param) {
        return process(param);
    }
}
