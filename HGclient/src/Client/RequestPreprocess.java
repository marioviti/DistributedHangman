package Client;

public class RequestPreprocess {
	
	public static final String usage = 	"USAGE: "+Message.typeCreateGame + " gameName(arg[0]) secretWor(arg[1]) (not null positive int)numOfGuessers(arg[2]), " +
			Message.typeJoinGame + "gameName(arg[0]), "+
			Message.typeLeaveGame + "gameName(arg[0]), " + Message.typeDeleteGame;
	
	public static String parseCommandLine(String command, Message req) {
		String[] arg = command.split("\\s+");
		switch(arg[0]) {
			case Message.typeCreateGame : {
				if(arg.length!=3)
					return usage;
				try {
					req.setHeader(Message.fieldType,arg[0]);
					req.setArgument(0,arg[1]);
					int num = Integer.parseInt(arg[2]);
					if(num <= 0) {
						return usage;
					}
					req.setArgument(1,num);
				} catch (Exception e) {
					return usage;
				}
				return null;
			}
			case Message.typeJoinGame : {
				if(arg.length!=2)
					return usage;
				try {
					req.setHeader(Message.fieldType,arg[0]);
					req.setArgument(0,arg[1]);
				} catch (Exception e) {
					return usage;
				}
				return null;
			}
			case Message.typeLeaveGame : {
				if(arg.length!=2)
					return usage;
				try {
					req.setHeader(Message.fieldType,arg[0]);
					req.setArgument(0,arg[1]);
				} catch (Exception e) {
					return usage;
				}
				return null;
			}
			case Message.typeDeleteGame: {
				req.setHeader(Message.fieldType,arg[0]);
				return null;
			}
			default: return usage;
		}
	}
}
