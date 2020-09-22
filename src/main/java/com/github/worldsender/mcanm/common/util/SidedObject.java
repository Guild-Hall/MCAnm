package com.github.worldsender.mcanm.common.util;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

// Due to java not allowing @<S super C, D> S downcast(...)@ we explicitly put S into the type...
// Treat this as sealed, don't extend from it!
public abstract class SidedObject<S, C extends S, D extends S> {
    private static class ClientDowncaster {
        static class ClientVariant<S, C extends S, D extends S> extends SidedObject<S, C, D> {
            final C theClientObject;
            ClientVariant(C clientObject) { this.theClientObject = clientObject; }
        }

        static final ClientDowncaster INSTANCE = new ClientDowncaster();

        <S, C extends S, D extends S> ClientVariant<S, C, D> construct(C clientObject) {
            return new ClientVariant<S, C, D>(clientObject);
        }

        @SuppressWarnings("unchecked")
        <S, C extends S, D extends S> ClientVariant<S, C, D> doCast(SidedObject<? super C, C, ?> obj) {
            return (ClientVariant<S, C, D>) obj;
        }
    }

    private static class ServerDowncaster {
        static class DedicatedServerVariant<S, C extends S, D extends S> extends SidedObject<S, C, D> {
            final D theServerObject;
            DedicatedServerVariant(D serverObject) { this.theServerObject = serverObject; }
        }

        static final ServerDowncaster INSTANCE = new ServerDowncaster();

        <S, C extends S, D extends S> DedicatedServerVariant<S, C, D> construct(D serverObject) {
            return new DedicatedServerVariant<S, C, D>(serverObject);
        }

        @SuppressWarnings("unchecked")
        <S, C extends S, D extends S> DedicatedServerVariant<S, C, D> doCast(SidedObject<? super D, ?, D> obj) {
            return (DedicatedServerVariant<S, C, D>) obj;
        }
    }

    private static <T> T runWithEvidence(Function<ClientDowncaster, T> client, Function<ServerDowncaster, T> server) {
        return DistExecutor.runForDist(() -> () -> client.apply(ClientDowncaster.INSTANCE), () -> () -> server.apply(ServerDowncaster.INSTANCE));
    }

    public static <T, C extends T, D extends T> SidedObject<T, C, D> of(Supplier<Supplier<C>> clientSupplier, Supplier<Supplier<D>> serverSupplier) {
        return runWithEvidence(c -> c.construct(clientSupplier.get().get()), d -> d.construct(serverSupplier.get().get()));
    }

    public <R, RC extends R, RD extends R> SidedObject<R, RC, RD> distMap(Function<C, RC> clientMap, Function<D, RD> serverMap) {
        return runWithEvidence(
            c -> c.construct(clientMap.apply(c.doCast(this).theClientObject)),
            d -> d.construct(serverMap.apply(d.doCast(this).theServerObject))
        );
    }

    public S get() {
        return runWithEvidence(c -> c.doCast(this).theClientObject, d -> d.doCast(this).theServerObject);
    }

    @OnlyIn(Dist.CLIENT)
    public C getClient() {
        return runWithEvidence(c -> c.doCast(this).theClientObject, d -> { throw new IllegalStateException("expected to run on client"); });
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public D getServer() {
        return runWithEvidence(c -> { throw new IllegalStateException("expected to run on server"); }, d -> d.doCast(this).theServerObject);
    }
}
