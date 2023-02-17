package net.isger.brick;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.isger.brick.bus.BusCommand;
import net.isger.brick.core.ConsoleManager;
import net.isger.brick.dao.EmployDAO;
import net.isger.brick.inject.Container;
import net.isger.brick.inject.ContainerBuilder;
import net.isger.brick.inject.ContainerProvider;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.stub.Stub;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.test.bean.Employ;
import net.isger.brick.test.bean.EmployModel;
import net.isger.util.Helpers;
import net.isger.util.sql.Page;

public class BrickStandardTest extends TestCase {

    private static final ConsoleManager MANAGER;

    static {
        // 初始环境
        MANAGER = new ConsoleManager();
        MANAGER.addContainerProvider(new ContainerProvider() {
            public void register(ContainerBuilder builder) {
                builder.factory(EmployDAO.class);
            }

            public boolean isReload() {
                return false;
            }
        });
        MANAGER.load();
    }

    public BrickStandardTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickStandardTest.class);
    }

    public void testBus() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        // 调用总线
        BusCommand cmd = new BusCommand();
        cmd.setEndpoint("client");
        cmd.setOperate(BusCommand.OPERATE_SEND);
        cmd.setParameter("test", "hello");
        cmd.setPayload(cmd);
        MANAGER.getConsole().execute(cmd);
    }

    public void testPlugin() {
        // 调用插件
        PluginCommand pcmd = new PluginCommand();
        pcmd.setDomain("test");
        pcmd.setName("goods");
        pcmd.setOperate("insert");
        pcmd.setParameter("id", "1");
        pcmd.setParameter("name", "first");
        MANAGER.getConsole().execute(pcmd);
        pcmd.setOperate("select");
        pcmd.setParameter("page", new Page());
        MANAGER.getConsole().execute(pcmd);
    }

    @SuppressWarnings("unchecked")
    public void testStub() {
        /* 作为系统平台支撑 */
        StubCommand cmd = new StubCommand();
        cmd.setDomain("test");
        try {
            testCreate(cmd);
        } catch (Exception e) {
        }
        testInsert(cmd);
        testDelete(cmd);
        testUpdate(cmd);
        testSelect(cmd);
        cmd.setDomain(null); // 取消指定存根操作（由存根管理器控制处理）
        cmd.setOperate(StubCommand.OPERATE_CREATE);
        Map<String, Object> config = new HashMap<String, Object>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("driverName", "org.h2.Driver");
        params.put("url", "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");
        params.put("user", "sa");
        config.put("parameters", params);
        cmd.setTransient(false); // 设置非暂态（交由容器托管，默认托管）
        cmd.setParameter("entrust", config); // 非暂态下必须设置托管存根域名
        MANAGER.getConsole().execute(cmd);
        Stub stub = ((Map<String, Stub>) cmd.getResult()).get("entrust");
        cmd.setOperate(StubCommand.OPERATE_SELECT);
        stub.select(cmd);
        showResult((Object[]) cmd.getResult());
        // MyBatis
        cmd.setDomain("testMyBatis");
        cmd.setTable(Employ.class);
        cmd.setCondition("select", new Object[] { "f%" });
        cmd.setOperate(StubCommand.OPERATE_SELECT);
        MANAGER.getConsole().execute(cmd);
        System.out.println(cmd.getResult());
        /* 作为系统外围支撑 */
        Container container = MANAGER.getConsole().getContainer();
        // 容器托管
        EmployDAO dao = container.getInstance(EmployDAO.class);
        dao.list();
        // 手工注入
        dao = container.inject(new EmployDAO());
        dao.list();
    }

    private void testCreate(StubCommand cmd) {
        cmd.setTable(new EmployModel());
        cmd.setOperate(StubCommand.OPERATE_CREATE);
        MANAGER.getConsole().execute(cmd);
    }

    private void testInsert(StubCommand cmd) {
        final Date date = new Date();
        // 单次
        EmployModel employ = new EmployModel("0", "zero", date);
        cmd.setTable(employ);
        cmd.setOperate(StubCommand.OPERATE_INSERT);
        MANAGER.getConsole().execute(cmd); // 方式一（使用bean或model实例）
        cmd.setTable(employ.modelName());
        cmd.setCondition(Helpers.wrap("id", "name", "input_time"), Helpers.wrap("1", "first", date));
        MANAGER.getConsole().execute(cmd); // 方式二（使用字符串描述）
        cmd.setTable(Employ.class);
        cmd.setCondition(Helpers.wrap(date, "2", "second"));
        MANAGER.getConsole().execute(cmd); // 方式三（使用配置文件）

        // 批量
        cmd.setCondition((Object) Helpers.group(Helpers.wraps(date, "3", "three"), Helpers.wraps(date, "4", "four")));
        MANAGER.getConsole().execute(cmd); // 方式一（配置）
        cmd.setTable(Helpers.wrap(new EmployModel("5", "five", date), new EmployModel("6", "six", date)));
        MANAGER.getConsole().execute(cmd); // 方式二（数组）
        cmd.setTable(new ArrayList<Employ>() {
            private static final long serialVersionUID = 1L;
            {
                this.add(new Employ("7", "seven", date));
                this.add(new Employ("8", "eight", date));
            }
        });
        MANAGER.getConsole().execute(cmd); // 方式二（集合）
        cmd.setTable(employ.modelName());
        cmd.setCondition(Helpers.wrap("id", "name", "input_time"), Helpers.group(Helpers.wraps("9", "nine", date), Helpers.wraps("10", "ten", date)));
        MANAGER.getConsole().execute(cmd); // 方式三（描述）
    }

    private void testDelete(StubCommand cmd) {
        // final Date date = new Date();
        // 单次
        EmployModel employ = new EmployModel("0");
        cmd.setTable(employ);
        cmd.setOperate(StubCommand.OPERATE_DELETE);
        MANAGER.getConsole().execute(cmd); // 方式一
        cmd.setTable(employ.modelName());
        cmd.setCondition(Helpers.wrap(Helpers.wrap("name"), Helpers.wrap("first")));
        MANAGER.getConsole().execute(cmd); // 方式二
        // try {
        // cmd.setCondition(
        // new Object[] { new String[] { "d1", "n1" },
        // new Object[] { date, "t%" },
        // "date < :d1 and name like :n1" });
        // manager.getConsole().execute(cmd); // 方式三
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        cmd.setTable(Employ.class);
        cmd.setCondition(Helpers.wrap("3"));
        MANAGER.getConsole().execute(cmd); // 方式四

        // 批量
        cmd.setCondition((Object) Helpers.group(Helpers.wraps("5"), Helpers.wraps("6")));
        MANAGER.getConsole().execute(cmd); // 方式一
    }

    private void testUpdate(StubCommand cmd) {
        // 单次
        EmployModel employ = new EmployModel("0", "zero");
        cmd.setTable(employ.modelName());
        cmd.setOperate(StubCommand.OPERATE_UPDATE);
        cmd.setCondition((Object) new Object[][] { { new String[] { "id", "name" }, new Object[] { "1", "first" } }, { new String[] { "id" }, new Object[] { "8" } } });
        MANAGER.getConsole().execute(cmd); // 方式一

    }

    private void testSelect(StubCommand cmd) {
        cmd.setTable(new Employ());
        cmd.setOperate(StubCommand.OPERATE_SELECT);
        MANAGER.getConsole().execute(cmd);
        showResult((Object[]) cmd.getResult());

        cmd.setTable(Employ.class);
        cmd.setCondition(Helpers.wrap("%n%", new Page(1, 3)));
        MANAGER.getConsole().execute(cmd);
        showResult((Object[]) cmd.getResult());
    }

    private void showResult(Object[] result) {
        System.out.println("-----------------------------------------");
        for (Object column : (Object[]) result[0]) {
            System.out.print(column + "\t\t");
        }
        System.out.println();
        System.out.println("-----------------------------------------");
        for (Object[] row : (Object[][]) result[1]) {
            for (int i = 0; i < row.length; i++) {
                System.out.print(row[i] + "\t\t");
            }
            System.out.println();
        }
        System.out.println("-----------------------------------------");
    }

}
