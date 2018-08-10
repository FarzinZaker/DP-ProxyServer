package ceraslabs.hogna.executor.commands;

public class CommandResult
{
    private int m_codeResult = CommandResultCodes.S_OK;

    public int  GetResultCode()          { return this.m_codeResult; }
    public void SetResultCode(int value) { this.m_codeResult = value; }
    
    public class CommandResultCodes
    {
    	public static final int S_OK = 0;
    	public static final int E_FAIL = -1;
    }
}
