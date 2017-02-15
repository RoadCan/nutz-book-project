package net.wendal.nutzbook.core.service.impl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.resource.Scans;

import net.wendal.nutzbook.core.bean.Permission;
import net.wendal.nutzbook.core.bean.Role;
import net.wendal.nutzbook.core.bean.User;
import net.wendal.nutzbook.core.service.AuthorityService;
import net.wendal.nutzbook.core.service.UserService;

@IocBean(name = "authorityService")
public class AuthorityServiceImpl implements AuthorityService {

	private static final Log log = Logs.get();

	@Inject
	protected Dao dao;
	
	protected UserService userService;

	public void initFormPackage(String pkg) {
		// 搜索@RequiresPermissions注解, 初始化权限表
		// 搜索@RequiresRoles注解, 初始化角色表
		final Set<String> permissions = new HashSet<String>();
		final Set<String> roles = new HashSet<String>();
		for (Class<?> klass : Scans.me().scanPackage(pkg)) {
			for (Method method : klass.getMethods()) {
				RequiresPermissions rp = method.getAnnotation(RequiresPermissions.class);
				if (rp != null && rp.value() != null) {
					for (String permission : rp.value()) {
						if (permission != null && !permission.endsWith("*"))
							permissions.add(permission);
					}
				}
				RequiresRoles rr = method.getAnnotation(RequiresRoles.class);
				if (rr != null && rr.value() != null) {
					for (String role : rr.value()) {
						roles.add(role);
					}
				}
			}
		}
		log.debugf("found %d permission", permissions.size());
		log.debugf("found %d role", roles.size());

		// 把全部权限查出来一一检查
		dao.each(Permission.class, null, new Each<Permission>() {
			public void invoke(int index, Permission ele, int length) throws ExitLoop, ContinueLoop, LoopException {
				permissions.remove(ele.getName());
			}
		});
		dao.each(Role.class, null, new Each<Role>() {
			public void invoke(int index, Role ele, int length) throws ExitLoop, ContinueLoop, LoopException {
				roles.remove(ele.getName());
			}
		});
		for (String permission : permissions) {
			addPermission(permission);
		}
		for (String role : roles) {
			addRole(role);
		}
	}

	public void checkBasicRoles() {
	    User admin = dao.fetch(User.class, "admin");
		// 检查一下admin的权限
		Role adminRole = dao.fetch(Role.class, "admin");
		if (adminRole == null) {
			adminRole = addRole("admin");
		}
		// admin账号必须存在与admin组
		String roleNames = admin.getRoleNames();
		if (roleNames == null)
		    roleNames = "";
		String[] tmp = Strings.splitIgnoreBlank(roleNames, ",");
		for (String role : tmp) {
            if ("admin".equals(role))
                return;
        }
		admin.setRoleNames("admin");
		dao.update(admin, "roleNames");
	}

	public void addPermission(String permission) {
		Permission p = new Permission();
		p.setName(permission);
		p.setUpdateTime(new Date());
		p.setCreateTime(new Date());
		dao.insert(p);
	}

	public Role addRole(String role) {
		Role r = new Role();
		r.setName(role);
		r.setUpdateTime(new Date());
		r.setCreateTime(new Date());
		return dao.insert(r);
	}
}