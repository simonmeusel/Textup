package de.simonmeusel.textup;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of textup plugin
 * 
 * @author Simon Meusel
 *
 */
public class Plugin extends JavaPlugin {

	HashMap<Integer, Boolean[][]> letters = new HashMap<>();
	
	int letterWidth;
	int letterHeight;

	@Override
	public void onEnable() {

		getConfig().options().copyDefaults(true);
		saveDefaultConfig();

		loadFont();

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// Only players can execute commands
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;

		if (command.getName().equalsIgnoreCase("textup")) {
			if (args.length == 0)
				return false;

			StringBuilder stringBuilder = new StringBuilder();

			for (String arg : args) {
				stringBuilder.append(arg);
			}

			String text = stringBuilder.toString();

			Location loc = player.getLocation();
			
			int sx = player.getLocation().getBlockX();
			int sy = player.getLocation().getBlockY();
			int sz = player.getLocation().getBlockZ();
			
			World world = player.getWorld();
			
			for (int letter : text.chars().toArray()) {
				if (letter > 256) continue;
				player.sendMessage("" + (char) letter);
				Boolean[][] blocks = letters.get(letter);
				for (int x = 0; x < blocks.length; x++) {
					for (int y = 0; y < blocks[x].length; y++) {
						if (blocks[x][y] == null) {
							world.getBlockAt(sx + x, sy - y, sz).setType(Material.STONE);
						}
					}
				}
				sx += letterWidth;
			}

			return true;
		}

		return false;
	}

	/**
	 * Get color values locatet in config
	 * 
	 * @param path
	 *            path to color values in config
	 * 
	 * @return Color with rgb values path.r path.g path.b
	 */

	private Color getColorByConfigPath(String path) {
		int r = getConfig().getInt(path + ".r");
		int g = getConfig().getInt(path + ".g");
		int b = getConfig().getInt(path + ".b");

		return new Color(r, g, b);
	}

	private void loadFont() {
		try {
			File fontFile = new File(getDataFolder(), getConfig().getString("font.path"));
			System.err.println(
					"You have to place the font image or adjust the path in the config.yml. For more infomation checkout the wiki.");
			if (!fontFile.isFile())
				return;
			BufferedImage font = ImageIO.read(fontFile);

			int width = getConfig().getInt("font.width");
			int heigth = getConfig().getInt("font.height");

			int lettersX = getConfig().getInt("font.lettersX");
			int lettersY = getConfig().getInt("font.lettersY");

			letterWidth = width / lettersX;
			letterHeight = heigth / lettersY;

			Color block = getColorByConfigPath("font.color.block");

			for (int x = 0; x < lettersX; x++) {
				for (int y = 0; y < lettersY; y++) {
					Boolean[][] blocks = new Boolean[letterWidth][letterHeight];
					for (int lx = 0; lx < letterWidth; lx++) {
						for (int ly = 0; ly < letterHeight; ly++) {
							int rgb = font.getRGB(lx + (x * letterWidth), ly + (y * letterHeight));
							Color color = new Color(rgb);
							if (block.equals(color)) {
								blocks[lx][ly] = true;
							}
						}
					}
					letters.put(x + lettersY * y, blocks);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
