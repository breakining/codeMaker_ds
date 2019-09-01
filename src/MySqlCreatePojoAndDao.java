import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;

/**
*
*	动态生成java类和配置文件
*/
public class MySqlCreatePojoAndDao {

	private Connection conn=null;
	private String url = "jdbc:mysql://192.168.10.210:3306/credit_application?useUnicode=true&characterEncoding=utf-8";
	private String userName ="bsit";
	private String userPwd="bsit_123";
	private PreparedStatement ps= null;
	private ResultSet rs =null;
	private ResultSetMetaData rm =null;
	private int isAllTable =0;
	private ArrayList<String> tables=new ArrayList<String>();//生成的表名
	private String pojopack="boer.cims.core.pojo";
	private String daopack ="boer.cims.core.dao";
	private String daoimplpack ="boer.cims.core.daoimpl";

	private String filePath="c:/userfiles/";	//生成到文件路径

	//连接数据库
	public MySqlCreatePojoAndDao() throws Exception{
			Class.forName("com.mysql.jdbc.Driver");
			conn =DriverManager.getConnection(url, userName, userPwd);

			isAllTable =1;//0查询库中所有用户表 1指定表名

			//需要生成的表
			tables.add("card");
	}

	//获取库中所有用户表
	private ArrayList<String> getDataTables() throws Exception{
		ArrayList<String> tmpList =new ArrayList<String>();
		String sql ="select table_name FROM information_schema.tables where table_schema='xies_cims'";
		conn =getConn();
		ps= conn.prepareStatement(sql);
		rs =ps.executeQuery();
		while(rs.next()){
			tmpList.add(rs.getString(1));
		}
		this.closeConn(conn, ps, rs);
		return tmpList;
	}
	private Connection getConn() throws Exception{
		if(conn == null){
			conn =DriverManager.getConnection(url, userName, userPwd);
		}
		return conn;
	}
	private void closeConn(Connection con,PreparedStatement ps,ResultSet rs) throws Exception{
		if (rs!= null) {
			rs.close();
			rs=null;
		}
		if (ps!= null) {
			ps.close();
			ps=null;
		}
		if (con!= null) {
			con.close();
			conn = null;
		}
	}
	private void getTablePojo() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/pojo/"+getTableOrColumn(tables.get(i), 1)+".java", reString);
		}
	}
	
	private void getTableDao() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableDaoString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/dao/I"+getTableOrColumn(tables.get(i), 1)+"Dao.java", reString);
		}
	}
	
	private void getTableDaoImpl() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String reString =this.getTableDaoImplString(sql,tables.get(i));
			//写入文件
			SaveFile.writeFile(filePath+"/daoimpl/"+getTableOrColumn(tables.get(i), 1)+"DaoImpl.java", reString);
		}
	}
	
	private String getTableDaoImplString(String sql,String tableName) throws Exception{
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+daoimplpack+";\n\n");
		sb.append("import "+pojopack+".*;\n");
		sb.append("import "+daopack+".*;\n");
		sb.append("/**\n");
		sb.append(" * 表："+tableName+" 对应daoImpl\n");
		sb.append(" */\n");
		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		rm=rs.getMetaData();
		String tmp="";
		if (rm.getColumnCount()>0) {
			int type =rm.getColumnType(1);
			if (type==Types.INTEGER) {
				tmp="Long";
			}else{
				tmp ="String";
			}
		}
		sb.append("public class "+tname+"DaoImpl extends BaseHapiDaoimpl<"+tname+", "+tmp+"> implements I"+tname+"Dao {\n\n");
		sb.append("   public "+tname+"DaoImpl(){\n");
		sb.append("      super("+tname+".class);\n");
		sb.append("   }\n");
		sb.append("}");
		this.closeConn(conn, ps, rs);
		return sb.toString();
	}
	
	private String getTableDaoString(String sql,String tableName) throws Exception{
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+daopack+";\n\n");
		sb.append("import "+pojopack+".*;\n");
		sb.append("/**\n");
		sb.append(" * 表："+tableName+" 对应dao\n");
		sb.append(" */\n");
		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		rm=rs.getMetaData();
		String tmp="";
		if (rm.getColumnCount()>0) {
			int type =rm.getColumnType(1);
			if (type==Types.INTEGER) {
				tmp="Long";
			}else{
				tmp ="String";
			}
		}
		sb.append("public interface I"+tname+"Dao extends BaseDao<"+tname+","+tmp+">{\n\n");
		sb.append("}");
		this.closeConn(conn, ps, rs);
		return sb.toString();
	}
	
	
	private String getTableString(String sql,String tableName) throws Exception{
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("package "+pojopack+";\n\n");
		sb.append("/**\n");
		sb.append(" * 数据库表名："+tableName+"\n");
		sb.append(" */\n");
		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		rm=rs.getMetaData();
		String tmp="";
		if (rm.getColumnCount()>0) {
			int type =rm.getColumnType(1);
			if (type==Types.INTEGER) {
				tmp="BaseBean";
			}else{
				tmp ="BaseStringBean";
			}
		}
		sb.append("public class "+tname+" extends "+tmp+" implements java.io.Serializable {\n\n");
		for (int i = 2; i <=rm.getColumnCount(); i++) {
			String cname=getTableOrColumn(rm.getColumnName(i), 0);
			if (rm.getColumnType(i) == Types.INTEGER) {
				sb.append("   private Integer "+cname+";\n");
			}else if(rm.getColumnType(i) == Types.NUMERIC ||rm.getColumnType(i)==Types.DECIMAL||rm.getColumnType(i) == Types.DOUBLE || rm.getColumnType(i) == Types.FLOAT){
				sb.append("   private Double "+cname+";\n");
			}else{
				sb.append("   private String "+cname+";\n");
			}
		}
		sb.append("\n");
		sb.append("   //默认构造方法\n");
		sb.append("   public "+tname+"(){\n");
		sb.append("      super();\n");
		sb.append("   }\n");
		
		sb.append("\n");
		sb.append("   //构造方法(手工生成)\n");
		sb.append("   \n");
		
		sb.append("\n");
		sb.append("  //get和set方法\n");
		for (int i = 2; i <=rm.getColumnCount(); i++) {
			String cname=getTableOrColumn(rm.getColumnName(i), 0);
			if (rm.getColumnType(i) == Types.INTEGER) {
				sb.append(this.createGetAndSetMethod("Integer", cname));
			}else if(rm.getColumnType(i) == Types.NUMERIC || rm.getColumnType(i)==Types.DECIMAL || rm.getColumnType(i) == Types.DOUBLE || rm.getColumnType(i) == Types.FLOAT){
				sb.append(this.createGetAndSetMethod("Double", cname));
			}else{
				sb.append(this.createGetAndSetMethod("String", cname));
			}
		}
		sb.append("}");
		this.closeConn(conn, ps, rs);
		return sb.toString();
	}
	private String createGetAndSetMethod(String type,String colsName){
		StringBuffer result =new StringBuffer();
		String tmp1 =colsName.substring(0,1).toUpperCase();
		String tmp2 =colsName.substring(1,colsName.length());
		String tmp3 ="a"+tmp1+tmp2;
		result.append("   public "+type+" get"+tmp1+tmp2+"(){\n");
		result.append("      return "+colsName+";\n");
		result.append("   }\n");
		result.append("\n");
		result.append("   public void set"+tmp1+tmp2+"("+type+" "+tmp3+"){\n");
		result.append("      this."+colsName+" = "+tmp3+";\n");
		result.append("   }\n");
		result.append("\n");
		return result.toString();
	}
	//对表名和列名进行转换 type==1为表名 否则为列名
	private String getTableOrColumn(String oldname,int type){
		String tmp =oldname.toLowerCase();
		String newStr="";
		if (type == 1) {
			String[] tbs =tmp.split("_");
			for (int i = 0; i < tbs.length; i++) {
				String strat=tbs[i].substring(0,1).toUpperCase();
				String end =tbs[i].substring(1,tbs[i].length());
				newStr+=strat+end;
			}
		}else{
			String[] cols =tmp.split("_");
			for (int i = 0; i < cols.length; i++) {
				if (i==0) {
					newStr+=cols[i];
				}else{
					String strat=cols[i].substring(0,1).toUpperCase();
					String end =cols[i].substring(1,cols[i].length());
					newStr+=strat+end;
				}
			}
		}
		return newStr;
		
	}
	
	private void getConfig() throws Exception{
		if (isAllTable == 0) {
			tables=this.getDataTables();
		}
		StringBuffer writeString=new StringBuffer();
		writeString.append("===========复制到dwr_base.xml文件=============\n");
		for (int i = 0; i < tables.size(); i++) {
			String dwrStr =this.getDwrString(tables.get(i));//dwr_base配置文件
			writeString.append(dwrStr);
		}
		writeString.append("==============================================\n\n");
		writeString.append("===========复制到spring-service.xml文件=============\n");
		for (int i = 0; i < tables.size(); i++) {
			String springStr =this.getSpringString(tables.get(i));//spring-service配置文件
			writeString.append(springStr);
		}
		writeString.append("==============================================\n\n");
		writeString.append("===========复制到boer.cims.erp.hbm.xml文件=============\n");
		for (int i = 0; i < tables.size(); i++) {
			String sql ="select * from "+tables.get(i);
			String hibString =this.getHibernateString(sql,tables.get(i));
			writeString.append(hibString);
		}
		writeString.append("==============================================\n\n");
		SaveFile.writeFile(filePath+"config.txt", writeString.toString());
	} 
	private String getDwrString(String tableName){
		StringBuffer sb =new StringBuffer();
		String tname= getTableOrColumn(tableName, 1);
		sb.append("<convert converter=\"hibernate3\" match=\""+pojopack+"."+tname+"\"/>\n");
		return sb.toString();
	}
	
	private String getSpringString(String tableName){
		StringBuffer sb= new StringBuffer();
		String tname =getTableOrColumn(tableName, 1);
		sb.append("<bean id=\""+getTableOrColumn(tableName,0)+"DaoImpl\" class=\""+daoimplpack+"."+tname+"DaoImpl\">\n");
		sb.append("    <property name=\"sessionFactory\" ref=\"sessionFactory\"/>\n</bean>\n");
		return sb.toString();
	}
	
	private String getHibernateString(String sql,String tableName) throws Exception{
		StringBuffer sb= new StringBuffer();
		String tname =getTableOrColumn(tableName, 1);
		conn=getConn();
		ps= conn.prepareStatement(sql);
		rs= ps.executeQuery();
		rm=rs.getMetaData();
		String tmp="";
		String tmp2=null;
		String oneCol="";
		if (rm.getColumnCount()>0) {
			oneCol =rm.getColumnName(1);
			int type =rm.getColumnType(1);
			if (type==Types.INTEGER) {
				tmp="long";
			}else{
				tmp ="java.lang.String";
			}
			if (rm.isAutoIncrement(1)) {
				tmp2="        <generator class=\"native\" />";
			}
		}
		sb.append("<class name=\""+pojopack+"."+tname+"\" table=\""+tableName.toLowerCase()+"\">\n");
		sb.append("    <id name=\"primaryKey\" type=\""+tmp+"\">\n");
		sb.append("        <column name=\""+oneCol.toLowerCase()+"\" />\n");
		if (tmp2 != null) {
			sb.append(tmp2+"\n");
		}
		sb.append("    </id>\n");
		for (int i = 2; i <=rm.getColumnCount(); i++) {
			String cname=getTableOrColumn(rm.getColumnName(i), 0);
			if (rm.getColumnType(i) == Types.INTEGER) {
				sb.append(this.CreateHib("java.lang.Integer", cname,rm.getColumnName(i)));
			}else if(rm.getColumnType(i) == Types.NUMERIC ||rm.getColumnType(i) == Types.DECIMAL||rm.getColumnType(i) == Types.DOUBLE || rm.getColumnType(i) == Types.FLOAT){
				sb.append(this.CreateHib("java.lang.Double", cname,rm.getColumnName(i)));
			}else{
				sb.append(this.CreateHib("java.lang.String", cname,rm.getColumnName(i)));
			}
		}
		sb.append("</class>\n\n");
		return sb.toString();
	}
	private String CreateHib(String type,String colName,String baseColName){
		StringBuffer sb =new StringBuffer();
		sb.append("    <property name=\""+colName+"\" type=\""+type+"\">\n");
		sb.append("        <column name=\""+baseColName.toLowerCase()+"\"/>\n");
		sb.append("    </property>\n");
		return sb.toString();
	}
	public static void main(String[] args) {
		try {
			System.out.println("==============start================");
			MySqlCreatePojoAndDao cp =new MySqlCreatePojoAndDao();
			System.out.println("===========创建pojo===================");
			cp.getTablePojo();
			System.out.println("===========创建dao====================");
			cp.getTableDao();
			System.out.println("===========创建daoImpl====================");
			cp.getTableDaoImpl();
			System.out.println("===========生成配置临时文件====================");
			cp.getConfig();
			System.out.println("=================end======================");
			
			cp.openExplorer(cp.filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 打开目录
	 * @param dir
	 */
	private void openExplorer(String dir){
		Runtime run = Runtime.getRuntime();   
	    try {   
	        // run.exec("cmd /k shutdown -s -t 3600");   
	        Process process = run.exec("cmd.exe /c start " + dir);   
	        InputStream in = process.getInputStream();     
	        while (in.read() != -1) {   
	            System.out.println(in.read());   
	        }   
	        in.close();   
	        process.waitFor();   
	    } catch (Exception e) {            
	        e.printStackTrace();   
	    }   

	}
}
