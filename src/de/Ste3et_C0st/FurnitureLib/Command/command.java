package de.Ste3et_C0st.FurnitureLib.Command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import de.Ste3et_C0st.FurnitureLib.Events.FurnitureClickEvent;
import de.Ste3et_C0st.FurnitureLib.Utilitis.JsonBuilder;
import de.Ste3et_C0st.FurnitureLib.Utilitis.ManageInv;
import de.Ste3et_C0st.FurnitureLib.Utilitis.JsonBuilder.ClickAction;
import de.Ste3et_C0st.FurnitureLib.Utilitis.JsonBuilder.HoverAction;
import de.Ste3et_C0st.FurnitureLib.main.FurnitureLib;
import de.Ste3et_C0st.FurnitureLib.main.FurnitureManager;
import de.Ste3et_C0st.FurnitureLib.main.Type.SQLAction;
import de.Ste3et_C0st.FurnitureLib.main.entity.fEntity;

public class command implements CommandExecutor, Listener{
	
	static FurnitureLib lib = FurnitureLib.getInstance();
	FurnitureManager manager = lib.getFurnitureManager();
	Plugin plugin;
	public static List<Player> playerList = new ArrayList<Player>();
	public static List<Player> manageList = new ArrayList<Player>();
	public static List<SubCommand> subCommands = new ArrayList<SubCommand>();
	public command(Plugin plugin){
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public static void addCommand(SubCommand subcommand){
		if(subCommands.contains(subcommand)) return;
		subCommands.add(subcommand);
	}
	
	@EventHandler
	public void onArmorRightClick(final FurnitureClickEvent e){
		if(playerList.contains(e.getPlayer())){
			e.setCancelled(true);
			Player p = e.getPlayer();
			p.sendMessage("§6Furniture Info about§e " + e.getID().getSerial());
			p.sendMessage("§6Plugin:§e " + e.getID().getPlugin());
			p.sendMessage("§6Type:§e " + e.getID().getProject());
			p.sendMessage("§6PublicMode:§e " + e.getID().getPublicMode().name().toLowerCase());
			p.sendMessage("§6Owner: §2" + e.getID().getPlayerName());
			p.sendMessage("§6PublicEventAccess: §e" + e.getID().getEventType().name().toLowerCase());
			p.sendMessage("§6ArmorStands: §e" + e.getID().getPacketList().size());
			p.sendMessage("§6FromDatabase: §c" + e.getID().isFromDatabase());
			p.sendMessage("§6Object Finish: §c" + e.getID().isFinish());
			p.sendMessage("§6Members: §e" + e.getID().getMemberList().size());
			p.sendMessage("§6SQL State: §e" + e.getID().getSQLAction().name().toLowerCase());
			p.sendMessage("§6Size: §e" + e.getID().getProjectOBJ().getLength()+":"+ e.getID().getProjectOBJ().getHeight()+":"+e.getID().getProjectOBJ().getWitdh());
			p.sendMessage("§6Blocks: §a" + e.getID().getBlockList().size());
			p.sendMessage("§6Project-Model: §e" + e.getID().getProjectOBJ().isEditorProject());
			if(e.getID().getUUID()!=null){
				OfflinePlayer player = Bukkit.getOfflinePlayer(e.getID().getUUID());
				if(player.hasPlayedBefore()&&!player.isOnline()){
					long mili1 = System.currentTimeMillis();
					long mili2 = player.getLastPlayed();
					long mili3 = mili1-mili2;
					SimpleDateFormat time = new SimpleDateFormat("D:HH:mm:ss.SSS");
			    	String timeStr = time.format(mili3);
			    	p.sendMessage("§6Player Offline: " + timeStr);
				}
			}
			
			for(fEntity stand : e.getID().getPacketList()){
				stand.setGlowing(true);
			}
			final SQLAction action = e.getID().getSQLAction();
			manager.updateFurniture(e.getID());
			playerList.remove(p);
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				
				@Override
				public void run() {
					for(fEntity stand : e.getID().getPacketList()){
						stand.setGlowing(false);
					}
					manager.updateFurniture(e.getID());
					e.getID().setSQLAction(action);
				}
			}, 20*5);
			
			
		}else if(manageList.contains(e.getPlayer())){
			e.setCancelled(true);
			Player p = e.getPlayer();
			manageList.remove(p);
			if(!e.getID().getUUID().equals(p.getUniqueId())){
				if(!lib.hasPerm(p, "furniture.admin") && !p.isOp() && !lib.hasPerm(p, "furniture.manage.other")){
					p.sendMessage(FurnitureLib.getInstance().getLangManager().getString("WrongOwner"));
					return;
				}
			}
			new ManageInv(p, e.getID());
			
		}
	}
	
	public static boolean noPermissions(CommandSender sender, String s){
		if(sender.isOp()) return true;
		if(lib.hasPerm(sender,"furniture.admin")) return true;
		if(!lib.hasPerm(sender,s)){
			sender.sendMessage(FurnitureLib.getInstance().getLangManager().getString("NoPermissions"));
			return false;
		}
		return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
			if(cmd.getName().equalsIgnoreCase("furniture")){
				Player p = null;
				if(sender instanceof Player){p=(Player) sender;}
				if(args.length==0){
					sendHelp(p);
					return true;
				}else{		
					switch (args[0]) {
					case "list": new listCommand(sender, cmd, arg2, args); return true;
					case "give": new giveCommand(sender, cmd, arg2, args); return true;
					case "recipe": new recipeCommand(sender, cmd, arg2, args); return true;
					case "debug": new debugCommand(sender, cmd, arg2, args); return true;
					case "manage": new manageCommand(sender, cmd, arg2, args); return true;
					case "remove": new removeCommand(sender, cmd, arg2, args); return true;
					case "spawn": new spawnCommand(sender, cmd, arg2, args); return true;
					case "purge": new purgeCommand(sender, cmd, arg2, args); return true;
					default:
						for(SubCommand sCmd : subCommands){
							if(sCmd.getSubcommand().equalsIgnoreCase(args[0])){
								sCmd.runCommand(sender, cmd, arg2, args);
								return true;
							}
						}
						if(args[0].equalsIgnoreCase("download")){
							p.sendMessage("§cThe FurnitureMaker plugin is not installed.");
							return true;
						}
						sendHelp(p);
						return true;
					}
				}
			}
		return false;
	}
	
	public static void sendHelp(Player player){
		if(player==null) return;
		
		String version = FurnitureLib.getInstance().getDescription().getVersion();
		String Author = FurnitureLib.getInstance().getDescription().getAuthors().get(0);
		String update = FurnitureLib.getInstance().getUpdater().getUpdate();
		
		String str = "";
		if(Bukkit.getPluginManager().isPluginEnabled("FurnitureMaker")){
			str = "\n§2Models";
		}
		
		
		if(!lib.hasPerm(player,"furniture.help")) return;
		JsonBuilder builder = new JsonBuilder("§7§m+--------------------§7[")
		.withText("§2Furniture").withHoverEvent(HoverAction.SHOW_TEXT, "§6§lVersion: §7" + version + "\n"
				+ "§6§lAuthor: §2" + Author +  update)
		.withText("§7]§m---------------------+\n")
		.withText("§6/furniture list §e(Option) §c(side)\n").withHoverEvent(HoverAction.SHOW_TEXT,"§6list all available furniture\n\n§cOption:\n§6Plugin\n§6Type\n§6World"+str).withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture list (Option)")
		.withText("§6/furniture give §e<furniture> §c(player) §d(Amount)\n").withHoverEvent(HoverAction.SHOW_TEXT,"§6if the §cPlayer not set:\n"
				+ "§cyou become a furniture\n" + 
				"§6if the Player set:\n" + 
				"§cgive the player one furniture").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture give <FURNITURE> (player)")
		.withText("§6/furniture debug \n").withHoverEvent(HoverAction.SHOW_TEXT,"§6You can become some information about\n§6abaout the furniture you are rightclicked").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture debug")
		.withText("§6/furniture manage \n").withHoverEvent(HoverAction.SHOW_TEXT,"§6You can config the furniture\n§6that you are rightclicked").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture manage")
		.withText("§6/furniture purge §e<Time>\n").withHoverEvent(HoverAction.SHOW_TEXT, "§6Marked all furnitures to remove from the database").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture purge <time>")
		.withText("§6/furniture recipe §e<type> §a(edit/remove)\n").withHoverEvent(HoverAction.SHOW_TEXT,"§6View recipe from a furniture").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture recipe <type>")
		.withText("§6/furniture remove §e<type/player/world/plugin/ID/Distance>\n").withHoverEvent(HoverAction.SHOW_TEXT,"§6You can remove furnitures with the type:"
		+ "\n§4Exemples: "
		+ "\n§ctype = tent1"
		+ "\n§cplayer = " + player.getName()
		+ "\n§cworld = " + player.getWorld().getName()
		+ "\n§cID = TOm4nvkoLW" 
		+ "\n§cDistance = 5").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture remove <type>")
		.withText("§6/furniture remove §elookat\n").withHoverEvent(HoverAction.SHOW_TEXT,"§6Remove a furniture at you looked").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture remove lookat")
		.withText("§6/furniture remove §eall\n").withHoverEvent(HoverAction.SHOW_TEXT,"§6Remove all furniture and reset database").withClickEvent(ClickAction.SUGGEST_COMMAND, "/furniture remove all");
		
		for(SubCommand commands : subCommands){
			builder.withText(commands.getCommand() + "\n").withHoverEvent(HoverAction.SHOW_TEXT, commands.getHoverText()).withClickEvent(ClickAction.SUGGEST_COMMAND, commands.getSuggest_Command());
		}
		
		builder.withText("\n").withText("§e§lTIP: §r§7Try to §e§nclick§7 or §e§nhover§7 the commands").
		withText("§7§m+--------------------------------------------------+").sendJson(player);
	}
}
