package users;

import Utils.DBHelpers;
import oracle.sql.OracleJdbc2SQLInput;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsersDAO implements Serializable {
    private List<UsersDTO> usersDTOList;

    public List<UsersDTO> getUsersDTOList() {
        return usersDTOList;
    }

    public int getUserList() throws SQLException, ClassNotFoundException {
        int total = 0;
        Connection con = null;
        PreparedStatement psm = null;
        ResultSet rs = null;
        try{
            con = DBHelpers.makeConnection();
            if(con!=null){
                String sql = "select username,password,fullName,admin,active " +
                        "from Users";
                psm = con.prepareStatement(sql);
                rs =psm.executeQuery();
                while(rs.next()){
                    if(this.usersDTOList==null)this.usersDTOList=new ArrayList<>();
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String fullName = rs.getString("fullName");
                    boolean admin = rs.getBoolean("admin");
                    boolean active = rs.getBoolean("active");
                    this.usersDTOList.add(new UsersDTO(username,password,fullName,admin,active));
                    total++;
                }
            }
        }finally {
            if(rs!=null)rs.close();
            if(psm!=null)psm.close();
            if(con!=null)con.close();
        }
        return total;
    }
}
