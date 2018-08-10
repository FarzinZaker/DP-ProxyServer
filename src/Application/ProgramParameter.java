package Application;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface ProgramParameter
{
	public String Name();
	public String Description();
}
