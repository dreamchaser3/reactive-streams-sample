import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Publisher<Integer> publisher = new Publisher<>() {
            private List<Integer> list = List.of(1, 2, 3, 4, 5);

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new Subscription() {
                    private Iterator<Integer> iterator = list.listIterator();

                    @Override
                    public void request(long l) {
                        executorService.execute(() -> {
                            long i = l;
                            try {
                                while (i-- > 0) {
                                    if (iterator.hasNext()) {
                                        subscriber.onNext(iterator.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
        Subscriber<Integer> subscriber = new Subscriber<>() {
            private Subscription subscription;
            public static final int requestSize = 2;
            private int i = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + ": onSubscribe");
                this.subscription = subscription;
                subscription.request(requestSize);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(Thread.currentThread().getName() + ": onNext: " + integer);
                if (--i <= 0) {
                    i = requestSize;
                    subscription.request(requestSize);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + ": onError: " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + ": onComplete");
            }
        };
        publisher.subscribe(subscriber);
    }
}
