/**
 * 
 */
package com.guzzservices.sso.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.guzz.util.StringUtil;

import com.guzzservices.rpc.CommandService;
import com.guzzservices.rpc.util.JsonUtil;
import com.guzzservices.sso.LoginException;
import com.guzzservices.sso.LoginUser;
import com.guzzservices.sso.stub.SSOInfo;

/**
 * 
 * 
 * 
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class CommandSSOServiceImpl extends AbstractSSOServiceImpl {
	
	private CommandService commandService ;
	
	public static final String COMMAND_LOGIN = "gs.sso.c.login" ;
	
	public static final String COMMAND_LOGOUT = "gs.sso.c.logout" ;
	
	public static final String COMMAND_GET_LOGIN_USER = "gs.sso.c.glu" ;
	
	public static final String COMMAND_CHECK_PASSWORD = "gs.sso.c.cpsw" ;
	
	public static final String COMMAND_QUERY_USER_INFO = "gs.sso.c.qui" ;
	
	protected SSOInfo internalLogout(HttpServletRequest request, HttpServletResponse response, String sessionId) {
		String json = null ;
		
		try {
			json = this.commandService.executeCommand(COMMAND_LOGOUT, sessionId);
		} catch (Exception e) {
			log.error("fail to logout session:" + sessionId, e) ;
		}
		
		if(StringUtil.notEmpty(json)){
			return JsonUtil.fromJson(json, SSOInfo.class) ;
		}
		
		return null ;
	}
	
	protected LoginUser internalGetLoginUser(HttpServletRequest request, HttpServletResponse response, CookieUser cu, String sessionId) {
		if(sessionId != null){
			String json = null ;
			
			try {
				json = this.commandService.executeCommand(COMMAND_GET_LOGIN_USER, sessionId);
			} catch (Exception e) {
				log.error("failed to read loginUser for session:" + sessionId, e) ;
			}
			
			if(StringUtil.notEmpty(json)){
				return JsonUtil.fromJson(json, LoginUser.class) ;
			}
		}
		
		return null ;
	}
	
	protected SSOInfo internalLogin(HttpServletRequest request, HttpServletResponse response, String oldSessionId, String userName, String password, int maxAge, boolean checkPassword) throws LoginException {
		String ip = request.getRemoteAddr() ;
		
		LoginCommandRequest r = new LoginCommandRequest() ;
		r.oldSessionId = oldSessionId ;
		r.userName = userName ;
		r.password = password ;
		r.IP = ip ;
		r.checkPassword = checkPassword ;
		r.maxAge = maxAge ;
		
		try {
			String json = this.commandService.executeCommand(COMMAND_LOGIN, JsonUtil.toJson(r));
			
			if(StringUtil.notEmpty(json)){
				return JsonUtil.fromJson(json, SSOInfo.class) ;
			}
			
		} catch (Exception e) {
			throw new LoginException(LoginException.SERVER_INTERNAL_ERROR, e.getMessage()) ;
		}
		
		return null ;
	}
	
	public void checkPassword(String IP, String userName, String password) throws LoginException {
		CheckPasswordCommandRequest r = new CheckPasswordCommandRequest() ;
		r.userName = userName ;
		r.password = password ;
		r.IP = IP ;
		
		int errorCode = -1 ;
		
		try {
			String result = this.commandService.executeCommand(COMMAND_CHECK_PASSWORD, JsonUtil.toJson(r));
			errorCode = Integer.parseInt(result) ;
		} catch (Exception e) {
			throw new LoginException(LoginException.SERVER_INTERNAL_ERROR, e.getMessage()) ;
		}
		
		if(SSOInfo.SUCCESS != errorCode){
			throw new LoginException(errorCode) ;
		}
		
		return ;
	}

	public Map<String, Object> queryUserInfo(String userName) throws LoginException {
		try {
			String result = this.commandService.executeCommand(COMMAND_QUERY_USER_INFO, userName);
			
			if(result == null){
				return null ;
			}
			
			return JsonUtil.fromJson(result, HashMap.class) ;
			
		} catch (Exception e) {
			throw new LoginException(LoginException.SERVER_INTERNAL_ERROR, e.getMessage()) ;
		}
	}
	
	public static class LoginCommandRequest{
		
		public String oldSessionId ;
		
		public String userName ;
		
		public String password ;
		
		public String IP ;
		
		public boolean checkPassword ;
		
		public int maxAge ;
		
	}
	
	public static class CheckPasswordCommandRequest{
		
		public String userName ;
		
		public String password ;
		
		public String IP ;
	}

	public CommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(CommandService commandService) {
		this.commandService = commandService;
	}

}

