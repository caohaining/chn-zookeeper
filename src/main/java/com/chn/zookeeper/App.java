package com.chn.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        //zk是有session概念的，没有线程池的概念
        //Watch：观察，回调
        //Watch的注册只发生在读类型调用，get、exites...
        //第一类：new zk 时候，传入的Watch,这个Watch,Session级别的，跟path、node没有关系
        final ZooKeeper zk = new ZooKeeper("192.168.150.11:2181,192.168.150.12:2181,192.168.150.13:2181,192.168.150.14:2181",
                3000, new Watcher() {
            //Watch的回调方法
            @Override
            public void process(WatchedEvent event) {
                Event.KeeperState state = event.getState();
                Event.EventType type = event.getType();
                String path = event.getPath();
                System.out.println("new ZK Watch:" + event.toString());

                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
                        countDownLatch.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                }

                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                }
            }
        });

        countDownLatch.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("ing.......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed.......");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        String pathName = zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        final Stat stat = new Stat();
        byte[] node = zk.getData("ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData Watch:" + event.toString());

                try {
                    //watch为true default Watch,被重新注册 new zk的那个Watch
                    zk.getData("/ooxx", this, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println(new String(node));

        //触发回调
        Stat stat1 = zk.setData("/ooxx", "newData".getBytes(), 0);
        //还会触发？
        Stat stat2 = zk.setData("/ooxx", "newData01".getBytes(), stat1.getVersion());

        System.out.println("---------async start---------");

        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("--------async call back-------");
                System.out.println(ctx.toString());
                System.out.println(new String(data));
            }
        },"abc");

        System.out.println("---------async stop---------");

        Thread.sleep(22222222);
    }
}
