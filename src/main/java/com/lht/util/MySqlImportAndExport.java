package com.lht.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
/**
 * 在进行导出的时候，需要注意命令语句的运行环境，如果已经将mysql安装路径下的bin加入到 
 * 系统的path变量中，那么在导出的时候可以直接使用命令语句，否则，就需要在执行命令语句的 
 * 时候加上命令所在位置的路径，即mysql安装路径下的bin下的mysqldump命令
 * @author 21
 *
 */
public class MySqlImportAndExport {
    public static void main(String args[]) throws IOException {
        InputStream is = MySqlImportAndExport.class.getClassLoader().getResourceAsStream("jdbc.properties");
        Properties properties = new Properties();
        properties.load(is);
//      MySqlImportAndExport.export(properties);//这里简单点异常我就直接往上抛 
        MySqlImportAndExport.importSql(properties);
    }

    /**
     * 根据属性文件的配置导出指定位置的指定数据库到指定位置 
     * @param properties
     * @throws IOException
     */
    public static void export(Properties properties) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String command = getExportCommand(properties);
        //这里简单一点异常我就直接往上抛
        runtime.exec(command);
    }

    /**
     * 导入sql
     * 根据属性文件的配置把指定位置的指定文件内容导入到指定的数据库中 
     * 在命令窗口进行mysql的数据库导入一般分三步走： 
     * 第一步是登到到mysql； mysql -uusername -ppassword -hhost -Pport -DdatabaseName;如果在登录的时候指定了数据库名则会 
     * 直接转向该数据库，这样就可以跳过第二步，直接第三步；  
     * 第二步是切换到导入的目标数据库；use importDatabaseName； 
     * 第三步是开始从目标文件导入数据到目标数据库；source importPath； 
     * @param properties
     * @throws IOException
     */
    public static void importSql(Properties properties) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        //因为在命令窗口进行mysql数据库的导入一般分三步走，所以所执行的命令将以字符串数组的形式出现
        //根据属性文件的配置获取数据库导入所需的命令，组成一个数组
        String cmdarray[] = getImportCommand(properties);
        //runtime.exec(cmdarray);//这里也是简单的直接抛出异常 
        Process process = runtime.exec(cmdarray[0]);
        //执行了第一条命令以后已经登录到mysql了，所以之后就是利用mysql的命令窗口 
        //进程执行后面的代码 
        OutputStream os = process.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(os);
        //命令1和命令2要放在一起执行 
        writer.write(cmdarray[1] + "\r\n" + cmdarray[2]);
        writer.flush();
        writer.close();
        os.close();
    }

    /**
     * 获取导出命令
     * 利配用属性文件提供的置来拼装命令语句
     * 在拼装命令语句的时候有一点是需要注意的：一般我们在命令窗口直接使用命令来 
     * 进行导出的时候可以简单使用“>”来表示导出到什么地方，即mysqldump -uusername -ppassword databaseName > exportPath， 
     * 但在Java中这样写是不行的，它需要你用-r明确的指出导出到什么地方，如： 
     * mysqldump -uusername -ppassword databaseName -r exportPath。 
     * @param properties
     * @return
     */
    private static String getExportCommand(Properties properties) {
        StringBuffer command = new StringBuffer();
        //用户名
        String username = properties.getProperty("jdbc.username");
        //用户密码
        String password = properties.getProperty("jdbc.password");
        //需要导出的数据库名
        String exportDatabaseName = properties.getProperty("jdbc.exportDatabaseName");
        //从哪个主机导出数据库，如果没有指定这个值，则默认取localhost
        String host = properties.getProperty("jdbc.host");
        //使用的端口号
        String port = properties.getProperty("jdbc.port");
        //导出路径
        String exportPath = properties.getProperty("jdbc.exportPath");

        //注意哪些地方要空格，哪些不要空格 //密码是用的小p，而端口是用的大P
        command.append("mysqldump -u").append(username).append(" -p").append(password)
                .append(" -h").append(host).append(" -P").append(port).append(" ").append(exportDatabaseName).append(" -r ").append(exportPath);
        return command.toString();
    }

    /**
     * 获取导入命令
     * 根据属性文件的配置，分三步走获取从目标文件导入数据到目标数据库所需的命令 
     * 如果在登录的时候指定了数据库名则会 
     * 直接转向该数据库，这样就可以跳过第二步，直接第三步；  
     * @param properties
     * @return
     */
    private static String[] getImportCommand(Properties properties) {
        //用户名
        String username = properties.getProperty("jdbc.username");
        //密码
        String password = properties.getProperty("jdbc.password");
        //导入的目标数据库所在的主机
        String host = properties.getProperty("jdbc.host");
        //使用的端口号
        String port = properties.getProperty("jdbc.port");
        //导入的目标数据库的名称
        String importDatabaseName = properties.getProperty("jdbc.importDatabaseName");
        //导入的目标文件所在的位置
        String importPath = properties.getProperty("jdbc.importPath");
        //第一步，获取登录命令语句 
        String loginCommand = new StringBuffer().append("mysql -u").append(username).append(" -p").append(password).append(" -h").append(host)
                .append(" -P").append(port).toString();
        //第二步，获取切换数据库到目标数据库的命令语句 
        String switchCommand = new StringBuffer("use ").append(importDatabaseName).toString();
        //第三步，获取导入的命令语句 
        String importCommand = new StringBuffer("source ").append(importPath).toString();
        //需要返回的命令语句数组 
        String[] commands = new String[] {loginCommand, switchCommand, importCommand};
        return commands;
    }

}