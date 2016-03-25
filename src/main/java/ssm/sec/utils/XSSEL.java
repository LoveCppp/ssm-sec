package ssm.sec.utils;

import org.apache.log4j.Logger;

import ssm.sec.filter.RequestWrapper;

public class XSSEL {
	private static Logger logger = Logger.getLogger(XSSEL.class);
	
	public static  String HTMLEncode(String val){
		logger.info("In EL func values ====>"+ val);
		if(val != null){
			val = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
					.replace("/", "&#x2F;").replace("'", "&#x27;");
			logger.info("Out EL func values ====>"+ val);
			return val;
		}else{
			return "";
		}
	}
}
