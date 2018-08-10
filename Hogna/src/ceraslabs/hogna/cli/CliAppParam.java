package ceraslabs.hogna.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface CliAppParam
{
	public String Name();
	public String Description();
}
