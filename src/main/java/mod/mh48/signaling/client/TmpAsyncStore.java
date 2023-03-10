package mod.mh48.signaling.client;

import java.util.function.Consumer;

public class TmpAsyncStore<C> {
    private C content;
    private boolean finished = false;

    private Consumer<C> onFinished;
    public void finish(C pContent){
        content = pContent;
        finished = true;
        if(onFinished != null){
            onFinished.accept(content);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public C getContent() {
        return content;
    }

    public void setOnFinished(Consumer<C> onFinished) {
        this.onFinished = onFinished;
        if(isFinished()){
            onFinished.accept(content);
        }
    }
}
