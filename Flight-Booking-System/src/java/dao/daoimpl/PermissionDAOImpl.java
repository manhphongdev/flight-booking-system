package dao.daoimpl;

import dao.interfaces.IPermissionDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Permission;
import utils.DBContext;

/**
 *
 * @author manhphong
 */
public class PermissionDAOImpl implements IPermissionDAO {

    public Permission permissionMapper(ResultSet rs) throws Exception {
        return new Permission(rs.getLong("permission_id"), rs.getNString("permission_name"), rs.getNString("description"));
    }

    @Override
    public Optional<Permission> findByID(Long id) {
        Permission permission = null;
        String sql = "SELECT * FROM [Permission] WHERE permission_id = ?";

        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    permission = permissionMapper(rs);
                    return Optional.of(permission);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Permission> findAll() {
        List<Permission> permissions = new LinkedList<>();

        String sql = "select * from [Permission]";

        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    permissions.add(permissionMapper(rs));
                } catch (Exception ex) {
                    Logger.getLogger(PermissionDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    @Override
    public Long insert(Permission entity) {
        StringBuilder sb = new StringBuilder("insert into [Permission] (permission_name, description)");
        sb.append("values(?,?)");
        Connection conn = null;
        try {
            conn = DBContext.getConn();
            PreparedStatement ps = conn.prepareStatement(sb.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
            conn.setAutoCommit(false);

            ps.setString(1, entity.getPermissonName());
            ps.setString(2, entity.getDescription());

            if (ps.executeUpdate() == 1) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    entity.setPermissionId(rs.getLong(1));
                    return entity.getPermissionId();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Insert failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        return null;
    }


    @Override
    public boolean deleteByID(Long id) {
        String sql = "delete from [Permission] where permission_id = ?";
        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 1) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Permission> selectByCondition() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean updateByPermissionName(Permission entity, Permission newEntity) {

        StringBuilder sb = new StringBuilder();
        sb.append("update [Permission] set permission_name = ?, description = ? ");
        sb.append("where permission_name = ?");

        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setString(1, newEntity.getPermissonName());
            ps.setString(2, newEntity.getDescription());
            ps.setString(3, entity.getPermissonName());

            if (ps.executeUpdate() == 1) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isPermissionNameExists(String permissionName) {

        String sql = "select count(*) from [Permission] where permission_name = ?";
        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, permissionName);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
        } catch (Exception ex) {
            Logger.getLogger(PermissionDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public Optional<Permission> findByName(String permissionName) {
        Permission role = null;
        String sql = "SELECT * FROM [Permission] WHERE permission_name = ?";

        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, permissionName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    role = permissionMapper(rs);
                    return Optional.of(role);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<String> findPermissionOfRole(String roleName) {
        List<String> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("select p.permission_name from Permission p ");
        sb.append("join Role_Has_Permission rp on rp.permission_id = p.permission_id ");
        sb.append("join Role r on r.role_id = rp.role_id ");
        sb.append("where role_name = ?");

        try (Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setString(1, roleName);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("permission_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) {
        PermissionDAOImpl dao = new PermissionDAOImpl();

        dao.deletePermissionOfRole("ADMIN", "create_user");
    }

    @Override
    public boolean deletePermissionOfRole(String roleName, String permissionName) {

        StringBuilder sb = new StringBuilder("Delete from Role_Has_Permission ");
        sb.append("where role_id = (select role_id from Role where role_name = ? ) ");
        sb.append("and permission_id = (select permission_id from Permission where permission_name = ?)");
        try(Connection conn = DBContext.getConn(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setString(1, roleName);
            ps.setString(2, permissionName);
            
            if(ps.executeUpdate() >0){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateByID(Long id, Permission entity) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
