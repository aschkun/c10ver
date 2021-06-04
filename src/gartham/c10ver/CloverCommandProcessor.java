package gartham.c10ver;

import static gartham.c10ver.economy.items.ItemBunch.of;
import static gartham.c10ver.utils.Utilities.format;
import static gartham.c10ver.utils.Utilities.listRewards;
import static gartham.c10ver.utils.Utilities.maxPage;
import static gartham.c10ver.utils.Utilities.paginate;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.alixia.javalibrary.JavaTools;
import org.alixia.javalibrary.strings.StringTools;
import org.alixia.javalibrary.util.Box;
import org.alixia.javalibrary.util.MultidimensionalMap;

import gartham.c10ver.changelog.Changelog.Version;
import gartham.c10ver.commands.CommandHelpBook.ParentCommandHelp;
import gartham.c10ver.commands.CommandInvocation;
import gartham.c10ver.commands.InputProcessor;
import gartham.c10ver.commands.MatchBasedCommand;
import gartham.c10ver.commands.SimpleCommandProcessor;
import gartham.c10ver.commands.consumers.InputConsumer;
import gartham.c10ver.commands.consumers.MessageInputConsumer;
import gartham.c10ver.commands.subcommands.ParentCommand;
import gartham.c10ver.commands.subcommands.SubcommandInvocation;
import gartham.c10ver.economy.Multiplier;
import gartham.c10ver.economy.Server;
import gartham.c10ver.economy.User;
import gartham.c10ver.economy.UserAccount;
import gartham.c10ver.economy.items.UserInventory;
import gartham.c10ver.economy.items.UserInventory.UserEntry;
import gartham.c10ver.economy.items.utility.crates.DailyCrate;
import gartham.c10ver.economy.items.utility.crates.LootCrateItem;
import gartham.c10ver.economy.items.utility.crates.MonthlyCrate;
import gartham.c10ver.economy.items.utility.crates.WeeklyCrate;
import gartham.c10ver.economy.items.utility.foodstuffs.Foodstuff;
import gartham.c10ver.economy.items.utility.itembomb.Bomb;
import gartham.c10ver.economy.items.utility.multickets.MultiplierTicket;
import gartham.c10ver.economy.questions.Question;
import gartham.c10ver.economy.questions.Question.Difficulty;
import gartham.c10ver.economy.server.ColorRole;
import gartham.c10ver.games.math.MathProblem;
import gartham.c10ver.games.math.MathProblem.AttemptResult;
import gartham.c10ver.games.math.MathProblemGenerator;
import gartham.c10ver.games.math.simple.SimpleMathProblemGenerator;
import gartham.c10ver.processing.commands.InventoryCommand;
import gartham.c10ver.processing.trading.TradeManager;
import gartham.c10ver.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class CloverCommandProcessor extends SimpleCommandProcessor {

	private final Clover clover;
	private final TradeManager tradeManager;

	public CloverCommandProcessor(Clover clover) {
		this.clover = clover;
		tradeManager = new TradeManager(clover);
		register(new InventoryCommand(clover, "inventory", "inv"));
	}

	{

//		register(new MatchBasedCommand("stats", "info") {
//
//			@Override
//			public void exec(CommandInvocation inv) {
//				net.dv8tion.jda.api.entities.User u;
//				if (inv.args.length > 0) {
//					String id = Utilities.parseMention(inv.args[0]);
//					if (id == null) {
//						inv.event.getChannel().sendMessage(
//								inv.event.getAuthor().getAsMention() + " ping who you want to see the stats of.")
//								.queue();
//						return;
//					} else {
//						try {
//							u = clover.getBot().retrieveUserById(id).complete();
//						} catch (NumberFormatException e) {
//							inv.event.getChannel()
//									.sendMessage(inv.event.getAuthor().getAsMention() + " that's not a valid mention.")
//									.queue();
//							return;
//						}
//						if (u == null) {
//							inv.event.getChannel()
//									.sendMessage(inv.event.getAuthor().getAsMention() + " that user couldn't be found.")
//									.queue();
//							return;
//						} else if (!clover.getEconomy().hasUser(u.getId())) {
//							inv.event.getChannel().sendMessage(u.getAsMention() + " doesn't have an account.").queue();
//							return;
//						}
//					}
//				} else if (!clover.getEconomy().hasUser(inv.event.getAuthor().getId())) {
//					inv.event.getChannel().sendMessage("You don't have an account.").queue();
//					return;
//				} else
//					u = inv.event.getAuthor();
//
//				EmbedBuilder eb = new EmbedBuilder();
//				eb.setAuthor(u.getAsTag() + "'s Stats!", null, u.getEffectiveAvatarUrl()).setColor(Color.blue);
//				// TODO Print stats.
//			}
//		});
		register(new MatchBasedCommand("tip") {

			@Override
			public void exec(CommandInvocation inv) {
				if (inv.args.length != 0)
					inv.event.getChannel()
							.sendMessage(
									inv.event.getAuthor().getAsMention() + " that command doesn't take any arguments.")
							.queue();
				else
					clover.getTiplist().get((int) (Math.random() * clover.getTiplist().size())).show(inv.event);
			}
		});
		register(new MatchBasedCommand("accolades") {

			@Override
			public void exec(CommandInvocation inv) {
				if (inv.args.length == 0) {
					StringBuilder desc = new StringBuilder();
					if (!clover.getEconomy().hasUser(inv.event.getAuthor().getId()))
						desc.append("You don't have any accolades...");
					else {
						User u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
						if (u.getAccolades().isEmpty())
							desc.append("You don't have any accolades...");
						else
							for (var le : u.getAccolades())
								desc.append('`').append(le.count()).append("x`\t").append(le.type.getIcon()).append(' ')
										.append(le.type.getName()).append('\n');
					}
					inv.event.getChannel()
							.sendMessage(new EmbedBuilder().setTitle(inv.event.getAuthor().getAsTag() + "'s Accolades")
									.setDescription(desc.toString()).build())
							.queue();
				} else if (inv.args.length != 1)
					inv.event.getChannel().sendMessage(
							inv.event.getAuthor().getAsMention() + " that command doesn't take more than 1 argument.")
							.queue();
				else {
					var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
					try {
						int pos = Integer.parseInt(inv.args[0]) - 1;
						if (pos < 0) {
							inv.event.getChannel()
									.sendMessage(
											inv.event.getAuthor().getAsMention() + ", you can't use a negative index (`"
													+ inv.args[0] + "`) when picking an accolade!")
									.queue();
							return;
						} else if (pos >= u.getAccolades().typeCount()) {
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + ", you don't have that many accolades!")
									.queue();
							return;
						} else {
							var entry = u.getAccolades().get(pos);
							inv.event.getChannel()
									.sendMessage(entry.type.getIcon() + " **" + entry.type.getName() + "** - *"
											+ entry.type.getDescription() + "*\n\nYou own `" + entry.count
											+ "` of these.\nReward: +" + Utilities.format(entry.type.getValue()))
									.queue();
						}
					} catch (NumberFormatException e) {
						inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
								+ ", that's not a valid index. Please pick which accolade you want more information about by index.")
								.queue();
					}

				}
			}
		});
		register(new MatchBasedCommand("daily") {
			@Override
			public void exec(CommandInvocation inv) {
				String userid = inv.event.getAuthor().getId();

				User u = clover.getEconomy().getUser(userid);
				if (u.timeSinceLastDaily().toDays() < 1)
					inv.event.getChannel()
							.sendMessage(inv.event.getAuthor().getAsMention() + ", you must wait `"
									+ Utilities.formatLargest(Duration.ofDays(1).minus(u.timeSinceLastDaily()), 3)
									+ "` before running that command.")
							.queue();
				else {
					u.dailyInvoked();
					var mult = u.calcMultiplier(inv.event.getGuild());
					var reward = u.rewardAndSave((long) (Math.random() * 25 + 10), mult);

					UserInventory invent = clover.getEconomy().getInventory(inv.event.getAuthor().getId());
					var rewards = of(new DailyCrate());
					invent.add(rewards).save();
					u.save();

					inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
							+ " is getting their daily rewards!\n\n**Rewards:**\n" + listRewards(reward, mult, rewards)
							+ "\nTotal Cloves: " + format(u.getAccount().getBalance())).queue();
				}

			}
		});
		register(new MatchBasedCommand("weekly") {
			@Override
			public void exec(CommandInvocation inv) {
				String userid = inv.event.getAuthor().getId();
				User u = clover.getEconomy().getUser(userid);
				if (u.timeSinceLastWeekly().toDays() < 7) {
					inv.event.getChannel()
							.sendMessage(inv.event.getAuthor().getAsMention() + ", you must wait `"
									+ Utilities.formatLargest(Duration.ofDays(7).minus(u.timeSinceLastWeekly()), 3)
									+ "` before running that command.")
							.queue();
				} else {
					u.weeklyInvoked();
					var mult = u.calcMultiplier(inv.event.getGuild());
					var amt = u.rewardAndSave((long) (Math.random() * 250 + 100), mult);

					UserInventory invent = clover.getEconomy().getInventory(inv.event.getAuthor().getId());
					var rewards = of(new WeeklyCrate());
					invent.add(rewards).save();
					u.save();

					inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
							+ " is getting their weekly rewards!\n\n**Rewards:**\n" + listRewards(amt, mult, rewards)
							+ "\nTotal Cloves: " + format(u.getAccount().getBalance())).queue();
				}
			}
		});
		register(new MatchBasedCommand("monthly") {
			@Override
			public void exec(CommandInvocation inv) {
				String userid = inv.event.getAuthor().getId();
				User u = clover.getEconomy().getUser(userid);
				if (u.timeSinceLastMonthly().toDays() < 30) {
					inv.event.getChannel()
							.sendMessage(inv.event.getAuthor().getAsMention() + ", you must wait `"
									+ Utilities.formatLargest(Duration.ofDays(30).minus(u.timeSinceLastMonthly()), 3)
									+ "` before running that command.")
							.queue();
				} else {
					u.monthlyInvoked();

					var mult = u.calcMultiplier(inv.event.getGuild());
					var amt = u.rewardAndSave((long) (Math.random() * 10000 + 4000), mult);

					UserInventory invent = clover.getEconomy().getInventory(inv.event.getAuthor().getId());
					var rewards = of(new MonthlyCrate());

					invent.add(rewards).save();
					u.save();

					inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
							+ " is getting their monthly rewards!!!\n\n**Rewards:**\n" + listRewards(amt, mult, rewards)
							+ "\nTotal Cloves: " + format(u.getAccount().getBalance())).queue();
				}
			}
		});

		register(new ParentCommand("open", "use") {

			private boolean openCrate(String cratetype, CommandInvocation inv) {
				if (clover.getEconomy().hasUser(inv.event.getAuthor().getId())) {
					var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
					@SuppressWarnings("unchecked")
					var crateEntry = (UserEntry<LootCrateItem>) u.getInventory().get("loot-crate");
					if (crateEntry != null)
						for (var is : crateEntry.getStacks())
							if (is.getItem().getCrateType().equalsIgnoreCase(cratetype)) {
								LootCrateItem lci = is.getItem();
								var rew = lci.open();
								for (var m : rew.getMultipliers())
									u.addMultiplier(m);
								var totalMult = u.calcMultiplier(inv.event.getGuild());
								var totalCloves = u.rewardAndSave(rew.getCloves(), totalMult);
								for (var i : rew.getItemList())
									u.getInventory().add(i).save();
								is.removeAndSave(BigInteger.ONE);
								u.save();

								inv.event.getChannel()
										.sendMessage(
												inv.event.getAuthor().getAsMention() + " is opening a **"
														+ lci.getCustomName() + "**!\n\n" + listRewards(rew,
																totalCloves, u.getAccount().getBalance(), totalMult))
										.queue();
								return true;
							}
				}
				return false;
			}

			{
				new Subcommand("bomb") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (inv.args.length == 0) {
							if (clover.getEconomy().hasUser(inv.event.getAuthor().getId())) {
								var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
								@SuppressWarnings("unchecked")
								var crateEntry = (UserEntry<Bomb>) u.getInventory().get("bomb");
								if (crateEntry != null) {
									crateEntry.get(0).getItem().consume(inv.event, clover);// Has to do the messaging on
																							// its own.
									crateEntry.get(0).removeAndSave(BigInteger.ONE);
									return;
								}
							}
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " you don't have any bombs. :(")
									.queue();
						} else
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " too many command arguments!")
									.queue();
					}
				};

				new Subcommand("crate", "loot-crate") {
					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (inv.args.length == 0)
							inv.event.getChannel()
									.sendMessage(
											inv.event.getAuthor().getAsMention() + " what crate do you want to open?")
									.queue();
						else if (inv.args.length == 1) {
							if (openCrate(inv.args[0], inv))
								return;
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " you don't have any crates of that type.")
									.queue();

						} else
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " too many args! Just provide a crate type!")
									.queue();
					}
				};

				new Subcommand("daily", "weekly", "monthly") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (!openCrate(inv.cmdName, inv))
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " you don't have any crates of that type.")
									.queue();
					}
				};

				new Subcommand("mult", "mult-ticket", "multiplier", "multiplier-ticket") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (inv.args.length == 0)
							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
									+ " please tell me which multiplier you'd like to use. You can refer to it by index.")
									.queue();
						else if (inv.args.length == 1) {
							if (!inv.event.isFromGuild())
								inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
										+ " you can only use multiplier tickets in a server (as they apply to the whole server).")
										.queue();
							else {
								if (clover.getEconomy().hasUser(inv.event.getAuthor().getId())) {
									var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
									@SuppressWarnings("unchecked")
									var multEntry = (UserEntry<MultiplierTicket>) u.getInventory()
											.get("multiplier-ticket");
									if (multEntry != null) {
										var ind = Integer.parseInt(inv.args[0]);
										if (ind > multEntry.getStacks().size()) {
											inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
													+ " you don't have that many different types of multiplier tickets.")
													.queue();
											return;
										} else if (ind < 1) {
											inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
													+ " you can't use a negative index...").queue();
											return;
										}

										var is = multEntry.get(ind - 1);
										var mti = is.getItem();
										Server serv = clover.getEconomy().getServer(inv.event.getGuild().getId());
										mti.use(clover, inv.event.getGuild(),
												clover.getEconomy().getUser(inv.event.getAuthor().getId()));
										var chn = serv.getGeneralChannel() == null ? inv.event.getTextChannel()
												: inv.event.getGuild().getTextChannelById(serv.getGeneralChannel());
										chn.sendMessage(inv.event.getAuthor().getAsMention() + " is using a **"
												+ Utilities.multiplier(mti.getAmount())
												+ "x** multiplier that lasts for **"
												+ Utilities.formatLargest(mti.getDuration(), 2) + "**.").queue();
										is.removeAndSave(BigInteger.ONE);

										serv.save();
									}

								}
							}
						} else
							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
									+ " too many args! Please provide just the index of the multiplier ticket you want to use.")
									.queue();
					}
				};
			}

			@Override
			protected void tailed(CommandInvocation inv) {
				if (inv.args.length == 0)
					inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
							+ " provide what item you want to open or use and try again.").queue();
				else if (inv.args.length == 1)
					if (clover.getEconomy().hasUser(inv.event.getAuthor().getId())) {
						var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
						var crateEntry = u.getInventory().get(inv.args[0]);
						if (crateEntry != null) {
							var is = crateEntry.get(0);
							if (is.getItem() instanceof Foodstuff) {
								is.removeAndSave(BigInteger.ONE);
								var lci = (Foodstuff) is.getItem();
								var mult = lci.getMultiplier();
								lci.consume(u);
								u.save();

								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention() + " you consumed some "
												+ lci.getEffectiveName() + " and received a multiplier: [**x"
												+ Utilities.multiplier(mult) + "**] for **"
												+ Utilities.format(Duration.ofMillis(lci.getTTL())) + "**!")
										.queue();
								return;
							}
						}
					}
				inv.event.getChannel()
						.sendMessage(inv.event.getAuthor().getAsMention()
								+ " you can't use that item! Either you don't have any of it, or it doesn't exist.")
						.queue();
			}
		});

		help.addCommand("mults", "Lists the multipliers that affect your rewards!", "mults", "multipliers");
		register(new MatchBasedCommand("mults", "multipliers") {

			@Override
			public void exec(CommandInvocation inv) {
				StringBuilder sb = new StringBuilder();
				BigDecimal pm;
				if (!clover.getEconomy().hasUser(inv.event.getAuthor().getId())) {
					sb.append(inv.event.getAuthor().getAsMention() + " you don't have any personal multipliers.\n");
					pm = BigDecimal.ONE;
				} else {
					var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
					var multipliers = u.getMultipliers();
					if (multipliers.isEmpty()) {
						sb.append(inv.event.getAuthor().getAsMention() + " you don't have any personal multipliers.\n");
						pm = BigDecimal.ONE;
					} else {
						sb.append(
								Utilities.strip(inv.event.getAuthor().getAsMention()) + "'s Personal Multipliers: \n");
						multDispHelper(sb, multipliers);
						sb.append("Total Personal Multiplier: [**x")
								.append(Utilities.multiplier(pm = u.getPersonalTotalMultiplier())).append("**]\n");
					}
				}

				if (inv.event.isFromGuild()) {
					var s = clover.getEconomy().getServer(inv.event.getGuild().getId());
					sb.append('\n');
					if (s.listMultipliers().isEmpty()) {
						sb.append("**" + Utilities.strip(inv.event.getGuild().getName())
								+ "** doesn't have any active multipliers.");
					} else {
						sb.append(Utilities.strip(inv.event.getGuild().getName()) + "'s Active Multipliers: \n");
						multDispHelper(sb, s.listMultipliers());
						sb.append("Total Server Multiplier: [**x")
								.append(Utilities.multiplier(s.getTotalServerMultiplier())).append("**]");
					}

					sb.append("\n\nGrand Total Multiplier: [**x" + Utilities.multiplier(pm) + "**] x [**x"
							+ Utilities.multiplier(s.getTotalServerMultiplier()) + "**] = __[**x"
							+ Utilities.multiplier(pm.multiply(s.getTotalServerMultiplier())) + "**]__");

				}

				inv.event.getChannel().sendMessage(sb.toString()).queue();

			}
		});

//		register(new ParentCommand("shop", "market") {
//
//			@Override
//			protected void tailed(CommandInvocation inv) {
//				if (inv.event.isFromGuild())
//					// if (!clover.getEconomy().hasServer(inv.event.getGuild().getId())) {
//						inv.event.getChannel().sendMessage("There is nothing in the shop yet...").queue();
//					} else {
//						EmbedBuilder eb = new EmbedBuilder();
//					inv.event.getChannel().sendMessage("There is nothing in the shop yet...").queue();
//					}
//				else
//					inv.event.getChannel().sendMessage("You must be in a guild to use that command.").queue();
//			}
//		});
		register(new ParentCommand("color", "color-role") {

			@Override
			protected void tailed(CommandInvocation inv) {
				if (inv.args.length == 0) {
					var m = inv.event.getChannel()
							.sendMessage(inv.event.getAuthor().getAsMention() + " what color do you want?");
					if (clover.getEconomy().hasServer(inv.event.getGuild().getId())) {
						Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
						if (!s.getColorRoles().isEmpty()) {
							EmbedBuilder eb = new EmbedBuilder();
							StringBuilder sb = new StringBuilder();
							sb.append("Available Color Roles:");
							for (var e : s.getColorRoles().entrySet())
								sb.append("\n<@&").append(e.getKey()).append("> ").append(e.getValue().getName())
										.append(" **").append(format(e.getValue().getCost())).append("**");
							sb.append("\n\n**NOTE:** You currently must pay **each** time you change your role.");
							eb.setDescription(sb);
							m.embed(eb.build()).queue();
							return;
						}
					}
					inv.event.getChannel().sendMessage("There are no color roles set up for this server yet.").queue();
				} else if (inv.args.length == 1) {
					if (clover.getEconomy().hasServer(inv.event.getGuild().getId())) {
						Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
						if (!s.getColorRoles().isEmpty()) {
							var u = clover.getEconomy().getAccount(inv.event.getAuthor().getId());
							for (ColorRole cr : s.getColorRoles().values())
								if (cr.getName().equalsIgnoreCase(inv.args[0])) {
									Role role;
									try {
										role = inv.event.getGuild().getRoleById(cr.getID());
									} catch (NumberFormatException e) {
										e.printStackTrace();
										inv.event.getChannel().sendMessage(
												"There is something wrong with that role. Please contact staff.")
												.queue();
										return;
									}
									if (role == null) {
										inv.event.getChannel().sendMessage(
												"There is something wrong with that role. Please contact staff.")
												.queue();
										return;
									}
									if (inv.event.getMember().getRoles().contains(role)) {
										inv.event.getChannel().sendMessage("You already have that role.").queue();
										return;
									}
									if (inv.event.getMember().getTimeBoosted() != null || u.withdraw(cr.getCost())) {
										u.save();
										List<Role> roles = new ArrayList<>(inv.event.getMember().getRoles());
										for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();)
											if (s.getColorRoles().containsKey(iterator.next().getId()))
												iterator.remove();
										roles.add(role);
										try {
											inv.event.getGuild().modifyMemberRoles(inv.event.getMember(), roles)
													.queue();
										} catch (PermissionException e) {
											inv.event.getChannel().sendMessage(
													"I'm missing permissions to do that. Please tell staff! (**Don't run the command again,** you might lose cloves.)")
													.queue();
											return;
										}
										inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
												+ " your color is now " + cr.getName() + "!").queue();
									} else
										inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
												+ " you don't have enough money to apply that role!").queue();
									return;
								}
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " couldn't find a role for that color.")
									.queue();
							return;
						}
					}
					inv.event.getChannel().sendMessage("There are no color roles set up for this server yet.").queue();
				} else {
					inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
							+ " there are no color roles set up for this server yet.").queue();
				}
			}
		});
		// TODO pay askdjflaskjhfd@Bob12987u1kmfdlskjflds 500
		register(new MatchBasedCommand("pay") {

			@Override
			public void exec(CommandInvocation inv) {
				if (inv.args.length != 2)
					inv.event.getChannel()
							.sendMessage("You need to specify two arguments, a user to pay, and an amount to pay.")
							.queue();
				else {
					BigInteger bi;
					try {
						bi = new BigInteger(inv.args[1]);
					} catch (NumberFormatException e) {
						inv.event.getChannel().sendMessage("Your second argument needs to be a number.").queue();
						return;
					}
					var mentionedUsers = inv.event.getMessage().getMentionedUsers();
					if (mentionedUsers.size() != 1) {
						inv.event.getChannel().sendMessage("You need to specify one user to pay money to.").queue();
						return;
					}

					if (inv.event.getAuthor().getId().equals(mentionedUsers.get(0).getId())) {
						inv.event.getChannel().sendMessage("You can't pay yourself money... :thinking:").queue();
						return;
					}

					UserAccount payer = clover.getEconomy().getAccount(inv.event.getAuthor().getId()),
							recip = clover.getEconomy().getAccount(mentionedUsers.get(0).getId());

					if (payer.pay(bi, recip))
						inv.event.getChannel()
								.sendMessage(inv.event.getAuthor().getAsMention() + ", you paid " + format(bi) + " to "
										+ mentionedUsers.get(0).getAsMention() + ". You now have "
										+ format(payer.getBalance()) + ".")
								.queue();
					else
						inv.event.getChannel()
								.sendMessage(
										inv.event.getAuthor().getAsMention() + ", you do not have enough money to pay `"
												+ bi + "` to " + mentionedUsers.get(0).getAsMention() + '.')
								.queue();
					payer.save();
					recip.save();
				}
			}
		});
		register(new MatchBasedCommand("bal", "balance") {

			@Override
			public void exec(CommandInvocation inv) {
				if (inv.args.length > 0) {
					String id = Utilities.parseMention(inv.args[0]);
					if (id == null)
						inv.event.getChannel().sendMessage(
								inv.event.getAuthor().getAsMention() + " ping who you want to check the balance of.")
								.queue();
					else {
						net.dv8tion.jda.api.entities.User u;
						try {
							u = clover.getBot().retrieveUserById(id).complete();
						} catch (NumberFormatException e) {
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " that's not a valid mention.")
									.queue();
							return;
						}
						if (u == null) {
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " that user couldn't be found.")
									.queue();
						} else {
							if (clover.getEconomy().hasAccount(u.getId())) {
								var bal = clover.getEconomy().getAccount(u.getId()).getBalance();
								inv.event.getChannel().sendMessage(u.getAsMention() + " has **" + format(bal) + "** (`"
										+ NumberFormat.getNumberInstance().format(bal) + "`)").queue();
							} else {
								inv.event.getChannel().sendMessage(u.getAsMention() + " doesn't have an account.")
										.queue();
							}
						}
					}
				} else {
					BigInteger bal = clover.getEconomy().getAccount(inv.event.getAuthor().getId()).getBalance();
					inv.event
							.getChannel().sendMessage(inv.event.getAuthor().getAsMention() + ", you have **"
									+ format(bal) + "** (`" + NumberFormat.getNumberInstance().format(bal) + "`)")
							.queue();
				}
			}
		});
		register(new MatchBasedCommand("baltop", "leaderboard") {

			@Override
			public void exec(CommandInvocation inv) {
				if (inv.event.isFromGuild()) {
					if (inv.args.length > 1) {
						inv.event.getChannel().sendMessage("Too many arguments.").queue();
						return;
					}
					List<Member> users = new ArrayList<>();
					inv.event.getGuild().findMembers(t -> !t.getUser().isBot()).onSuccess(t -> {
						for (Member m : t) {
							int search = Collections
									.binarySearch(users, m,
											((Comparator<Member>) (o1, o2) -> clover.getEconomy().getUser(o1.getId())
													.getAccount().getBalance().compareTo(clover.getEconomy()
															.getUser(o2.getId()).getAccount().getBalance()))
																	.reversed());
							users.add(search < 0 ? -search - 1 : search, m);
						}
					});
					int page;
					PAGE_PARSER: if (inv.args.length == 1) {
						try {
							if ((page = Integer.parseInt(inv.args[0])) > 0)
								break PAGE_PARSER;
						} catch (NumberFormatException e) {
						}
						inv.event.getChannel()
								.sendMessage(inv.event.getAuthor().getAsMention() + ", that's not a valid page!")
								.queue();
						return;
					} else
						page = 1;

					int maxpage = maxPage(10, users);
					if (page > maxpage) {
						inv.event.getChannel()
								.sendMessage(inv.event.getAuthor().getAsMention() + " there "
										+ (maxpage == 1 ? "is only `1` page" : "are only `" + maxpage + "` pages")
										+ " of people in the leaderboard!")
								.queue();
						return;
					}

					EmbedBuilder eb = new EmbedBuilder();
					eb.setAuthor("Server Leaderboard", null, inv.event.getGuild().getIconUrl());
					StringBuilder sb = new StringBuilder();

					List<Member> paginate = paginate(page, 10, users);
					for (int i = 0; i < paginate.size(); i++) {
						var u = paginate.get(i);
						sb.append("`#" + (page * 10 - 9 + i) + "` " + u.getUser().getName() + "#"
								+ u.getUser().getDiscriminator() + ": "
								+ format(clover.getEconomy().getAccount(u.getId()).getBalance()) + "\n");
					}
					eb.addField("Page " + page + " Ranking", sb.toString(), false);
					eb.setFooter("Showing page " + page + " in the server leaderboard.");

					inv.event.getChannel().sendMessage(eb.build()).queue();
				} else
					inv.event.getChannel().sendMessage("Please run this command in a server.").queue();
			}
		});

		register(new ParentCommand("quiz") {

			class AskedQuiz {
				Question question;
				InputConsumer<MessageReceivedEvent> msgcons;
				InputConsumer<MessageReactionAddEvent> reaccons;

				public AskedQuiz(Question question, InputConsumer<MessageReceivedEvent> msgcons,
						InputConsumer<MessageReactionAddEvent> reaccons) {
					this.question = question;
					this.msgcons = msgcons;
					this.reaccons = reaccons;
				}

			}

			MultidimensionalMap<AskedQuiz> questionMap = new MultidimensionalMap<>(2);
			{
				new Subcommand("list") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (!clover.isDev(inv.event.getAuthor())) {
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " you can't use that command.")
									.queue();
							return;
						}
						var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
						if (u.getQuestions().isEmpty()) {
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " you don't have any questions stored.")
									.queue();
						} else {
							int page;
							if (inv.args.length == 0)
								page = 1;
							else {
								try {
									page = Integer.parseInt(inv.args[0]);
								} catch (NumberFormatException e) {
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention()
													+ " this is not a valid question number: `"
													+ Utilities.strip(inv.args[0]) + '`')
											.queue();
									return;
								}
								if (page < 0) {
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention()
													+ " this is not a valid question number: `"
													+ Utilities.strip(inv.args[0]) + '`')
											.queue();
									return;
								}
							}
							List<Question> questions = paginate(page, 5, u.getQuestions());
							int mp = maxPage(5, u.getQuestions());
							if (questions == null) {
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention() + " there "
												+ (mp == 1 ? "is only `1` page!" : "are only `" + mp + "` pages!"))
										.queue();
							} else {
								StringBuilder sb = new StringBuilder();
								sb.append("**Page ").append(page).append(" of questions**\n");
								int temp = page;
								temp--;
								temp *= 5;
								temp++;
								for (var q : questions)
									sb.append("\n`Q").append(temp++).append("` ").append(q.getDifficulty())
											.append(" - ").append(format(q.getValue()));
								if (page == mp)
									sb.append("\n\nEnd of question list.");
								else
									sb.append("\n\nUse `quiz list ").append(page + 1).append("` to see the next page.");
								inv.event.getChannel().sendMessage(sb).queue();
							}
						}
					}
				};
				new Subcommand("new", "make", "create") {
					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (!clover.isDev(inv.event.getAuthor())) {
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " you can't use that command.")
									.queue();
							return;
						}
						if (inv.args.length != 2)
							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
									+ ", you need to provide a *value* and a *difficulty* in that command. After you do that, you'll get prompted for the question.")
									.queue();
						else {

							BigInteger value;
							try {
								value = new BigInteger(inv.args[0]);
							} catch (NumberFormatException e) {
								inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
										+ " your first argument should be a number! (It's the value of the question.)")
										.queue();
								return;
							}

							Difficulty difficulty;
							try {
								difficulty = Difficulty.valueOf(inv.args[1].toUpperCase());
							} catch (IllegalArgumentException e) {
								inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
										+ " your second argument should be a difficulty.").queue();
								return;
							}

							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
									+ " looks good so far! Please send your question as a message: ").queue();
							MessageInputConsumer inp = (event, eventHandler, ic) -> {
								event.getChannel().sendMessage(
										event.getAuthor().getAsMention() + " your question has been registered!")
										.queue();
								Question q = new Question(event.getMessage().getContentRaw(), value, difficulty);
								User user = clover.getEconomy().getUser(event.getAuthor().getId());
								user.getQuestions().add(q);
								user.save();
								eventHandler.scheduleForRemoval(ic);
								return true;
							};
							clover.getEventHandler().getMessageProcessor()
									.registerInputConsumer(inp.filter(inv.event.getAuthor(), inv.event.getChannel()));
						}
					}
				};
				new Subcommand("view") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (inv.args.length == 1) {
							int numb;
							try {
								numb = Integer.parseInt(inv.args[0]) - 1;
							} catch (NumberFormatException e) {
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention()
												+ " this is not a valid question number: `"
												+ Utilities.strip(inv.args[0]) + '`')
										.queue();
								return;
							}
							if (numb < 0)
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention()
												+ " this is not a valid question number: `"
												+ Utilities.strip(inv.args[0]) + '`')
										.queue();
							else {
								var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
								var questions = u.getQuestions();
								if (numb >= questions.size())
									inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
											+ " you only have `" + questions.size() + "` questions!").queue();
								else {
									var q = questions.get(numb);
									inv.event.getChannel()
											.sendMessage(new EmbedBuilder().setColor(switch (q.getDifficulty()) {
									case EASY:
										yield Color.green;
									case MEDIUM:
										yield Color.yellow;
									case HARD:
										yield Color.red;
									default:
										yield Color.black;
									}).setAuthor(
											"Question #" + (numb + 1) + " [" + Utilities.format(q.getValue()) + ']')
													.addField("\u200B", q.getQuestion(), false).build())
											.queue();
								}
							}
						} else if (inv.args.length == 0)
							inv.event.getChannel()
									.sendMessage(
											inv.event.getAuthor().getAsMention() + " tell me which question to show.")
									.queue();
						else
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " too many arguments! >:(")
									.queue();
					}
				};
				new Subcommand("delete", "remove", "del", "rem") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (inv.args.length == 1) {
							int numb;
							try {
								numb = Integer.parseInt(inv.args[0]) - 1;
							} catch (NumberFormatException e) {
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention()
												+ " this is not a valid question number: `"
												+ Utilities.strip(inv.args[0]) + '`')
										.queue();
								return;
							}
							if (numb < 0)
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention()
												+ " this is not a valid question number: `"
												+ Utilities.strip(inv.args[0]) + '`')
										.queue();
							else {
								var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
								var questions = u.getQuestions();
								if (numb >= questions.size())
									inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
											+ " you only have `" + questions.size() + "` questions!").queue();
								else {
									inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
											+ " removed question " + (numb + 1) + '.').queue();
									var q = questions.remove(numb);
									@SuppressWarnings("unchecked")
									Map<String, AskedQuiz> dim = (Map<String, AskedQuiz>) questionMap
											.readDim(inv.event.getAuthor().getId());
									if (dim != null)
										for (var e : dim.entrySet())
											if (e.getValue().question == q) {
												questionMap.remove(inv.event.getAuthor().getId(), e.getKey());
												clover.getEventHandler().getMessageProcessor()
														.removeInputConsumer(e.getValue().msgcons);
												clover.getEventHandler().getReactionAdditionProcessor()
														.removeInputConsumer(e.getValue().reaccons);
											}
									u.save();
								}
							}
						} else if (inv.args.length == 0)
							inv.event.getChannel()
									.sendMessage(
											inv.event.getAuthor().getAsMention() + " tell me which question to delete.")
									.queue();
						else
							inv.event.getChannel()
									.sendMessage(inv.event.getAuthor().getAsMention() + " too many arguments! >:(")
									.queue();
					}
				};
			}

			@Override
			protected void tailed(CommandInvocation inv) {
				if (!clover.isDev(inv.event.getAuthor())) {
					inv.event.getChannel()
							.sendMessage(inv.event.getAuthor().getAsMention() + " you can't use that command.").queue();
					return;
				}
				if (inv.args.length != 1)
					inv.event.getChannel().sendMessage("You need to tell me which question you want to use.").queue();
				else {
					int numb;
					try {
						numb = Integer.parseInt(inv.args[0]) - 1;
					} catch (NumberFormatException e) {
						inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
								+ " this is not a valid question number: `" + Utilities.strip(inv.args[0]) + '`')
								.queue();
						return;
					}
					if (questionMap.contains(inv.event.getAuthor().getId(), inv.event.getChannel().getId())) {
						inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
								+ " this will close your previous question. Continue? (yes/N)").queue();
						clover.getEventHandler().getMessageProcessor()
								.registerInputConsumer(((MessageInputConsumer) (event, eventHandler, consumer) -> {
									switch (event.getMessage().getContentRaw().toLowerCase()) {
									case "y":
									case "yes":
										questionMap.remove(inv.event.getAuthor().getId(),
												inv.event.getChannel().getId());
										inv.event.getChannel()
												.sendMessage(
														inv.event.getAuthor().getAsMention() + " question rescinded.")
												.queue();
										clover.getEventHandler().getMessageProcessor().removeInputConsumer(consumer);
										return true;
									case "n":
									case "no":
										inv.event.getChannel().sendMessage("Alright.").queue();
										clover.getEventHandler().getMessageProcessor().removeInputConsumer(consumer);
										return true;
									default:
										return false;
									}
								}).filter(inv.event.getAuthor().getId(), inv.event.getChannel().getId()));
					} else if (numb < 0)
						inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
								+ " this is not a valid question number: `" + Utilities.strip(inv.args[0]) + '`')
								.queue();
					else {
						var u = clover.getEconomy().getUser(inv.event.getAuthor().getId());
						var questions = u.getQuestions();
						if (numb >= questions.size())
							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention() + " you only have `"
									+ questions.size() + "` questions!").queue();
						else {
							var q = questions.get(numb);
							Box<InputConsumer<MessageReactionAddEvent>> reactionHandler = new Box<>();
							Box<InputConsumer<MessageReceivedEvent>> messageHandler = new Box<>();
							reactionHandler.value = (event, eventHandler, consumer) -> {
								if (event.getChannel().getId().equals(inv.event.getChannel().getId())
										&& event.getUserId().equals(inv.event.getAuthor().getId())
										&& event.getReactionEmote().isEmoji()
										&& event.getReactionEmote().getEmoji().equals("\u2705")) {
									var user = event.getChannel().retrieveMessageById(event.getMessageId()).complete()
											.getAuthor();
									var u1 = clover.getEconomy().getUser(user.getId());
									var mult = u1.calcMultiplier(event.getGuild());
									var rewards = u1.rewardAndSave(q.getValue(), mult);

									String m = Utilities.multiplier(mult);

									String msg = user.getAsMention() + ", you got the question right and earned "
											+ rewards + " for answering it!";
									if (m != null)
										msg += "\n\nMultiplier: **" + m + "**.";

									questionMap.remove(inv.event.getAuthor().getId(), inv.event.getChannel().getId());

									clover.getEventHandler().getReactionAdditionProcessor()
											.removeInputConsumer(consumer);
									clover.getEventHandler().getMessageProcessor()
											.removeInputConsumer(messageHandler.value);

									event.getChannel().sendMessage(msg).queue();
									return true;
								}
								return false;
							};
							messageHandler.value = (event, eventHandler, consumer) -> {
								if (event.getChannel().getId().equals(inv.event.getChannel().getId())
										&& event.getAuthor().getId().equals(inv.event.getAuthor().getId())) {
									CommandInvocation ci = clover.getCommandParser()
											.parse(event.getMessage().getContentRaw(), event);
									if (ci == null)
										return false;
									if (ci.cmdName.equalsIgnoreCase("accept")) {
										if (ci.args.length == 0) {
											event.getChannel().sendMessage(event.getAuthor().getAsMention()
													+ " whose answer do you want to accept?").queue();
											return true;
										}
										var fid = Utilities.parseMention(ci.args[0]);
										if (fid == null)
											event.getChannel().sendMessage(event.getAuthor().getAsMention()
													+ " ping whoever got the right answer in the `accept` command.")
													.queue();
										else {
											var user = event.getJDA().retrieveUserById(fid).complete();
											if (user == null) {
												event.getChannel().sendMessage(event.getAuthor().getAsMention()
														+ " that person couldn't be found.").queue();
												return true;
											}
											var u1 = clover.getEconomy().getUser(fid);

											var mult = u1.calcMultiplier(event.getGuild());
											var rewards = u1.reward(q.getValue(), mult);

											String m = Utilities.multiplier(mult);

											String msg = user.getAsMention()
													+ ", you got the question right and earned " + format(rewards)
													+ " for answering it!";
											if (m != null)
												msg += "\n\nMultiplier: **" + m + "**.";

											questionMap.remove(inv.event.getAuthor().getId(),
													inv.event.getChannel().getId());

											clover.getEventHandler().getReactionAdditionProcessor()
													.removeInputConsumer(reactionHandler.value);
											clover.getEventHandler().getMessageProcessor()
													.removeInputConsumer(consumer);

											event.getChannel().sendMessage(msg).queue();

										}
										return true;
									} else if (ci.cmdName.equalsIgnoreCase("cancel")) {
										event.getChannel()
												.sendMessage(event.getAuthor().getAsMention() + " question cancelled.")
												.queue();
										questionMap.remove(inv.event.getAuthor().getId(),
												inv.event.getChannel().getId());
										clover.getEventHandler().getReactionAdditionProcessor()
												.removeInputConsumer(reactionHandler.value);
										clover.getEventHandler().getMessageProcessor().removeInputConsumer(consumer);
										return true;
									}
								}
								return false;
							};
							clover.getEventHandler().getReactionAdditionProcessor()
									.registerInputConsumer(reactionHandler.value);
							clover.getEventHandler().getMessageProcessor().registerInputConsumer(messageHandler.value);
							questionMap.put(new AskedQuiz(q, messageHandler.value, reactionHandler.value),
									inv.event.getAuthor().getId(), inv.event.getChannel().getId());
							inv.event.getChannel().sendMessage(new EmbedBuilder().setColor(switch (q.getDifficulty()) {
							case EASY:
								yield Color.green;
							case MEDIUM:
								yield Color.yellow;
							case HARD:
								yield Color.red;
							default:
								yield Color.black;
							}).setAuthor("Question #" + (numb + 1) + " [" + Utilities.format(q.getValue()) + ']')
									.addField("\u200B", q.getQuestion(), false).build()).queue();
						}
					}
				}
			}
		});
		register(new ParentCommand("setup") {

			private final ParentCommandHelp setupHelp = help.addParentCommand("setup",
					"Allows you to set up and configure the bot to work with a server. (__You must be a Clover Officer to access this command!__)");
			private final ParentCommandHelp configHelp = setupHelp.addParentSubcommand("configure",
					"Configures specific settings for Clover. This will let you change settings, set new settings, and clear old settings.",
					"config");
			{
				setupHelp.addSubcommand("register",
						"Registers a server (and possibly its general channel) with Clover. This must be run in the server that should be linked to Clover.",
						"setup register [general-channel]", "create", "new");
				setupHelp.addSubcommand("view",
						"Allows you to view configuration settings that Clover has stored for this server.",
						"setup view");

				new Subcommand("register", "create", "new") {
					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (!inv.event.isFromGuild()) {
							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
									+ " you can only use that command in a server.").queue();
						} else {
							if (clover.getEconomy().hasServer(inv.event.getGuild().getId())) {
								inv.event.getChannel().sendMessage(
										inv.event.getAuthor().getAsMention() + " this server is already registered.")
										.queue();
							} else {
								var serv = clover.getEconomy().getServer(inv.event.getGuild().getId());
								if (inv.args.length == 1) {
									Object o;
									String cm = Utilities.parseChannelMention(inv.args[0]);
									if (cm == null) {
										inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
												+ " that's not a valid channel ID.").queue();
										return;
									}
									try {
										o = inv.event.getGuild().getTextChannelById(cm);
									} catch (NumberFormatException e) {
										inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
												+ " that's not a valid channel ID.").queue();
										return;
									}
									if (o == null) {
										inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
												+ " that's not a valid channel ID.").queue();
										return;
									}
									serv.setGeneralChannel(cm);
								} else if (inv.args.length != 0) {
									inv.event.getChannel().sendMessage(
											inv.event.getAuthor().getAsMention() + " too many arguments provided.")
											.queue();
								}
								inv.event.getChannel().sendMessage("Registered this server.").queue();
								serv.save();
							}
						}
					}
				};

				new Subcommand("view") {

					@Override
					protected void tailed(SubcommandInvocation inv) {
						if (inv.event.isFromGuild())
							if (inv.args.length != 0)
								inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
										+ " that subcommand doesn't take arguments.").queue();
							else if (clover.getEconomy().hasServer(inv.event.getGuild().getId())) {
								StringBuilder sb = new StringBuilder();
								Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
								sb.append("**Server Info:**");
								if (s.getGeneralChannel() != null)
									sb.append("\nGeneral Channel: <#").append(s.getGeneralChannel()).append('>');
								if (s.getSpamChannel() != null)
									sb.append("\nSpam Channel: <#").append(s.getSpamChannel()).append('>');
								if (s.getGamblingChannel() != null)
									sb.append("\nGambling Channel: <#").append(s.getGamblingChannel()).append('>');
								if (!s.getIgnoredInvites().isEmpty()) {
									sb.append("\nIgnored Invites:");
									for (var e : s.getIgnoredInvites())
										sb.append("\n`").append(e).append('`');
								}
								if (!s.getColorRoles().isEmpty()) {
									sb.append("\nColor Roles:");
									for (var e : s.getColorRoles().entrySet())
										sb.append("\n<@&").append(e.getKey()).append("> ")
												.append(e.getValue().getName()).append(" **")
												.append(format(e.getValue().getCost())).append("**");
								} else if (s.getIgnoredInvites().isEmpty() && s.getGeneralChannel() == null
										&& s.getSpamChannel() == null && s.getGamblingChannel() == null)
									sb.append("\nNothing has been configured for this server yet.");
								EmbedBuilder eb = new EmbedBuilder().setDescription(sb.toString());
								inv.event.getChannel().sendMessage(eb.build()).queue();
							} else
								inv.event.getChannel().sendMessage("This server is not yet registered with me.")
										.queue();
						else
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " you can only run that in a server.")
									.queue();
					}
				};

				new Subcommand("configure", "config") {

					{
						configHelp.addSubcommand("set",
								"Sets the value of a specific property. Currently you can set the general, gambling, or spam channel.\nFor example, `setup config set general #main` will configure clover to use `#main` as the general channel.",
								"setup ... set (property) (value)");
						configHelp.addSubcommand("clear",
								"Clears the value of a property. Currently, you can clear the general, spam, and gambling channel properties, or the color role list property.\nExample: `setup config clear color-roles`",
								"setup ... clear (property)");
						configHelp.addSubcommand("add",
								"Adds a value to a property which holds multiple elements, (like the color role list).",
								"setup ... add (property) (...values)");
						configHelp.addSubcommand("remove",
								"Removes a SINGLE ELEMENT from a property that contains multiple elements. Ex: `setup config remove color-roles @Red`.\nTo completely clear a property, use the `clear` subcommand instead of the `remove` subcommand.",
								"setup ... remove (property) (value)");

						new Subcommand("set") {

							@Override
							protected void tailed(SubcommandInvocation inv) {
								if (inv.args.length == 0) {
									inv.event.getChannel()
											.sendMessage(
													inv.event.getAuthor().getAsMention() + " what do you want to set?")
											.queue();
								} else if (inv.args.length == 1) {
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention() + " provide a value.")
											.queue();
								} else if (inv.args.length == 2) {
									Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
									switch (inv.args[0]) {
									case "general-channel":
									case "general":
										CHANP: {
											String cm = Utilities.parseChannelMention(inv.args[1]);
											if (cm == null)
												break CHANP;
											Object o;
											try {
												o = inv.event.getGuild().getTextChannelById(cm);
											} catch (NumberFormatException e) {
												break CHANP;
											}
											if (o == null)
												break CHANP;
											s.setGeneralChannel(cm);
											inv.event.getChannel().sendMessage("General channel set to <#" + cm + ">.")
													.queue();
											break;
										}
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that's not a valid channel.")
												.queue();
										return;
									case "gambling-channel":
									case "gambling":
										CHANP: {
											String cm = Utilities.parseChannelMention(inv.args[1]);
											if (cm == null)
												break CHANP;
											Object o;
											try {
												o = inv.event.getGuild().getTextChannelById(cm);
											} catch (NumberFormatException e) {
												break CHANP;
											}
											if (o == null)
												break CHANP;
											s.setGamblingChannel(cm);
											inv.event.getChannel().sendMessage("Gambling channel set to <#" + cm + ">.")
													.queue();
											break;
										}
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that's not a valid channel.")
												.queue();
										return;
									case "spam-channel":
									case "spam":
										CHANP: {
											String cm = Utilities.parseChannelMention(inv.args[1]);
											if (cm == null)
												break CHANP;
											Object o;
											try {
												o = inv.event.getGuild().getTextChannelById(cm);
											} catch (NumberFormatException e) {
												break CHANP;
											}
											if (o == null)
												break CHANP;
											s.setSpamChannel(cm);
											inv.event.getChannel().sendMessage("Spam channel set to <#" + cm + ">.")
													.queue();
											break;
										}
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that's not a valid channel.")
												.queue();
										return;
									default:
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that isn't a valid property.")
												.queue();
										return;
									}
									s.save();
								} else
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention() + " too many arguments.")
											.queue();
							}
						};

						new Subcommand("clear") {

							@Override
							protected void tailed(SubcommandInvocation inv) {
								if (inv.args.length == 0)
									inv.event.getChannel().sendMessage(
											inv.event.getAuthor().getAsMention() + " what do you want to clear?")
											.queue();
								else if (inv.args.length == 1) {
									Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
									switch (inv.args[0]) {
									case "general-channel":
									case "general":
										s.setGeneralChannel(null);
										inv.event.getChannel().sendMessage("Unregistered the general channel.").queue();
										break;
									case "gambling-channel":
									case "gambling":
										s.setGamblingChannel(null);
										inv.event.getChannel().sendMessage("Unregistered the gambling channel.")
												.queue();
										break;
									case "spam-channel":
									case "spam":
										s.setSpamChannel(null);
										inv.event.getChannel().sendMessage("Unregistered the spam channel.").queue();
										break;
									case "color-roles":
									case "color-role":
									case "colorrole":
										s.setColorRoles(new HashMap<>());
										inv.event.getChannel().sendMessage("Cleared the color role list.").queue();
										break;
									case "ignored-invites":
									case "ignored-invs":
									case "ignoredinvs":
									case "ignoredinvites":
									case "ignored-invite":
									case "ignored-inv":
									case "ignoredinv":
									case "ignoredinvite":
										s.getIgnoredInvites().clear();
										inv.event.getChannel().sendMessage("Cleared the ignored invites list.").queue();
										break;
									default:
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that isn't a valid property.")
												.queue();
										return;
									}
									s.save();
								} else
									inv.event.getChannel().sendMessage(
											inv.event.getAuthor().getAsMention() + " what do you want to clear?")
											.queue();
							}
						};

						new Subcommand("add") {

							@Override
							protected void tailed(SubcommandInvocation inv) {
								if (inv.args.length == 0) {
									inv.event.getChannel().sendMessage(
											inv.event.getAuthor().getAsMention() + " what do you want to add to?")
											.queue();
								} else if (inv.args.length == 1) {
									inv.event.getChannel()
											.sendMessage(
													inv.event.getAuthor().getAsMention() + " provide a value to add.")
											.queue();
								} else {
									Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
									switch (inv.args[0]) {
									case "ignored-invites":
									case "ignored-invs":
									case "ignoredinvs":
									case "ignoredinvites":
									case "ignored-invite":
									case "ignored-inv":
									case "ignoredinv":
									case "ignoredinvite":
										if (inv.args.length == 2) {
											if (s.getIgnoredInvites().isEmpty())
												s.setIgnoredInvites(new HashSet<>());
											s.getIgnoredInvites().add(inv.args[1]);
											inv.event.getChannel().sendMessage("Added the invite successfully.")
													.queue();
											break;
										} else {
											inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
													+ " you gave too many args. >:(").queue();
											return;
										}
									case "color-roles":
									case "color-role":
									case "colorrole":
										if (inv.args.length == 2) {
											inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
													+ " provide a name and cost for the role.").queue();
											return;
										} else if (inv.args.length == 3) {
											inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
													+ " provide a cost to for the role.").queue();
											return;
										} else if (inv.args.length == 4) {
											ROLEP: {
												String cm = Utilities.parseChannelMention(inv.args[1]);
												if (cm == null)
													cm = Utilities.strip(inv.args[1]);
												Object o;
												try {
													o = inv.event.getGuild().getRoleById(cm);
												} catch (NumberFormatException e) {
													break ROLEP;
												}
												if (o == null)
													break ROLEP;
												BigInteger cost;
												try {
													cost = new BigInteger(inv.args[3]);
												} catch (NumberFormatException e) {
													inv.event.getChannel().sendMessage(
															"Your last argument when adding a color role must be a cost.")
															.queue();
													return;
												}
												if (s.getColorRoles().isEmpty())
													s.setColorRoles(new HashMap<>());
												s.getColorRoles().put(cm,
														new ColorRole(Utilities.strip(inv.args[2]), cm, cost));
												inv.event.getChannel().sendMessage("Added the role successfully.")
														.queue();
												break;
											}
											inv.event.getChannel().sendMessage(
													inv.event.getAuthor().getAsMention() + " that's not a valid role.")
													.queue();
											return;
										} else {
											inv.event.getChannel().sendMessage(
													inv.event.getAuthor().getAsMention() + " too many arguments.")
													.queue();
											return;
										}
									default:
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that isn't a valid property.")
												.queue();
										return;
									}
									s.save();
								}
							}
						};

						new Subcommand("remove") {

							@Override
							protected void tailed(SubcommandInvocation inv) {
								if (inv.args.length == 0)
									inv.event.getChannel().sendMessage(
											inv.event.getAuthor().getAsMention() + " what do you want to remove from?")
											.queue();
								else if (inv.args.length == 1)
									inv.event.getChannel().sendMessage(
											inv.event.getAuthor().getAsMention() + " provide a value to remove.")
											.queue();
								else if (inv.args.length == 2) {
									Server s = clover.getEconomy().getServer(inv.event.getGuild().getId());
									switch (inv.args[0]) {
									case "ignored-invites":
									case "ignored-invs":
									case "ignoredinvs":
									case "ignoredinvites":
									case "ignored-invite":
									case "ignored-inv":
									case "ignoredinv":
									case "ignoredinvite":
										if (s.getIgnoredInvites().contains(inv.args[1])) {
											s.getIgnoredInvites().remove(inv.args[1]);
											inv.event.getChannel()
													.sendMessage("Removed `" + Utilities.strip(inv.args[1])
															+ "` from the list of ignored invites.")
													.queue();
											break;
										} else {
											inv.event.getChannel()
													.sendMessage(inv.event.getAuthor().getAsMention()
															+ " that invite was not in the list of ignored invites.")
													.queue();
											return;
										}
									case "color-roles":
									case "color-role":
									case "colorrole":
										ROLEP: {
											String cm = Utilities.parseRoleMention(inv.args[1]);
											if (cm == null)
												cm = inv.args[1];
											Object o;
											try {
												o = inv.event.getGuild().getRoleById(cm);
											} catch (NumberFormatException e) {
												break ROLEP;
											}
											if (o == null)
												break ROLEP;
											if (s.getColorRoles().containsKey(cm)) {
												s.getColorRoles().remove(cm);
												inv.event.getChannel().sendMessage("Removed the role successfully.")
														.queue();
											} else {
												inv.event.getChannel()
														.sendMessage("That role is not in the color roles list.")
														.queue();
												return;
											}
											break;
										}
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that's not a valid role.")
												.queue();
										return;
									default:
										inv.event.getChannel().sendMessage(
												inv.event.getAuthor().getAsMention() + " that isn't a valid property.")
												.queue();
										return;
									}
									s.save();
								} else
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention() + " too many arguments.")
											.queue();
							}
						};
					}

					@Override
					protected void tailed(SubcommandInvocation inv) {
						configHelp.print(inv.event.getChannel());
					}
				};
			}

			public boolean match(CommandInvocation inv) {
				return super.match(inv) && clover.isDev(inv.event.getAuthor());
			}

			@Override
			protected void tailed(CommandInvocation inv) {
				setupHelp.print(inv.event.getChannel());
			}
		});

		register(new MatchBasedCommand("trade") {

			@Override
			public void exec(CommandInvocation inv) {
				if (!inv.event.isFromGuild()) {
					inv.event.getChannel().sendMessage(
							inv.event.getAuthor().getAsMention() + " you may only start a trade inside a server.")
							.queue();
					return;
				}
				// Trading blocks all other commands so that we don't have to account for things
				// such as a user consuming an item after it has been listed as a trade item,
				// but before the trade has succeeded.
//				if (trades.containsKey(inv.event.getAuthor().getId())) {
//					var trade = trades.get(inv.event.getAuthor().getId());
//					if (trade.initiated) {
//						inv.event.getChannel()
//								.sendMessage(inv.event.getAuthor().getAsMention() + " you are already in a trade!")
//								.queue();
//					}
//				}

				if (inv.args.length == 0)
					inv.event.getChannel().sendMessage(
							inv.event.getAuthor().getAsMention() + " you need to @mention whom you want to trade with.")
							.queue();
				else if (inv.args.length > 1)
					inv.event.getChannel()
							.sendMessage(
									inv.event.getAuthor().getAsMention() + " that command only takes one argument!")
							.queue();
				else {
					String id = Utilities.parseMention(inv.args[0]);
					if (id == null)
						inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
								+ " please @mention whomever you want to trade with.").queue();
					else {
						var u = inv.event.getGuild().getMemberById(id);
						if (u == null)
							inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
									+ " that user is not a member of this server. :(").queue();
						else if (tradeManager.participating(u.getId())) {
							var t = tradeManager.getTrade(u.getId());
							if (t.isAccepted()) {
								if (u.getUser().getId().equals(t.getRecip().getEcouser().getUserID()))
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention()
													+ " someone already requested " + u.getEffectiveName()
													+ " to trade. Please wait until that is finished or cancelled.")
											.queue();
								else
									inv.event.getChannel()
											.sendMessage(inv.event.getAuthor().getAsMention() + ", "
													+ u.getEffectiveName() + " is waiting on a trade already. ")
											.queue();
							} else
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention() + ", " + u.getEffectiveName()
												+ " is already engaged in a trade. Please wait 'till they're finished.")
										.queue();
						} else if (u.getUser().isBot())
							inv.event.getChannel()
									.sendMessage(
											inv.event.getAuthor().getAsMention() + " you can't start trades with bots!")
									.queue();
						else if (u.getUser().equals(inv.event.getAuthor()))
							inv.event.getChannel().sendMessage(
									inv.event.getAuthor().getAsMention() + " you can't start a trade with yourself!")
									.queue();
						else {

							var recipient = clover.getEconomy().getUser(u.getUser().getId());
							var requester = clover.getEconomy().getUser(inv.event.getAuthor().getId());
							tradeManager.open(requester, recipient, inv.event.getTextChannel());
							// This will automatically add itself as a MessageInputConsumer to the msg input
							// consumer processor associated with clover.
							// This works because of the fact that the tradeManager that this processor
							// class has keeps a reference to the Clover object needed by these Trade
							// objects. The Trade object is a MessageInputConsumer and can be unlinked at
							// any time by calling its #end() method.
						}
					}
				}
			}
		});

		register(new MatchBasedCommand("changelog", "updates", "changes") {

			private String print(Version version) {
				StringBuilder str = new StringBuilder();

				str.append("`").append(version.getVerstr()).append('`');
				if (version.getTitle() != null)
					str.append(" - **").append(version.getTitle().strip()).append("**");
				str.append(" Changelog");
				for (var c : version.getChanges())
					str.append('\n').append(switch (c.getType()) {
					case ADDITION -> '+';
					case CHANGE -> '~';
					case FIX -> '*';
					case REMOVAL -> '-';
					default -> "?";
					}).append(' ').append(c.getContent());
				return str.toString();
			}

			@Override
			public void exec(CommandInvocation inv) {
				var cl = clover.getChangelog();
				if (cl == null) {
					inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
							+ ", the changelog is malformed (there are some mistakes in it!) so I can't display it right now. :(")
							.queue();
					return;
				}
				switch (inv.args.length) {
				case 0:
					var ver = cl.getVersions().get(cl.getVersions().size() - 1);// Latest version.
					inv.event.getChannel().sendMessage(print(ver)).queue();
					break;
				case 1:
					for (var v : cl.getVersions())
						if (v.getVerstr().equals(inv.args[0])) {
							inv.event.getChannel().sendMessage(print(v)).queue();
							return;
						}
					int page;
					try {
						page = Integer.parseInt(inv.args[0]);
					} catch (NumberFormatException e) {
						inv.event.getChannel().sendMessage(inv.event.getAuthor().getAsMention()
								+ ", no version or page found: " + Utilities.strip(inv.args[0]) + '.').queue();
						return;
					}
					var vers = Utilities.paginate(page, 10, cl.getVersions());
					if (vers == null) {
						inv.event.getChannel()
								.sendMessage(inv.event.getAuthor().getAsMention() + " that's not a valid page.")
								.queue();
						return;
					}
					var sb = new StringBuilder();
					for (var v : vers) {
						sb.append('`').append(v.getVerstr()).append('`');
						if (v.getVerstr() != null)
							sb.append(" - **").append(v.getTitle()).append("**");
						sb.append("\n");
					}
					inv.event.getChannel().sendMessage("Page `" + page + "` of Versions:\n" + sb).queue();
					break;
				default:
					inv.event.getChannel().sendMessage(
							inv.event.getAuthor().getAsMention() + " you provided too many arguments in that command.")
							.queue();
				}
			}
		});

		register(new MatchBasedCommand("math") {

			Timer timer = new Timer(true);

			class MathState {
				BigInteger value = BigInteger.valueOf(500);
				double diff = 1;

				double upgrade() {
					value = new BigDecimal(value).multiply(BigDecimal.valueOf(diff += Math.random() * .5 + .5))
							.toBigInteger().add(value);
					return diff;
				}

				Set<String> players = new HashSet<>(1);
				MathProblem problem;
				MessageInputConsumer mic;
				TimerTask ts;
				Instant inst;
			}

			Map<String, MathState> channelToProblemMap = new HashMap<>();
			private final MathProblemGenerator mpg = new SimpleMathProblemGenerator();

			/**
			 * Returns whether or not there's a game currently running in the channel that
			 * the provided CommandInvocation was invoked in. This command does also clear
			 * old entries from the map.
			 * 
			 * @param inv The invocation.
			 * @return <code>true</code> if there's a game running in the channel,
			 *         <code>false</code> otherwise.
			 */
			private boolean running(CommandInvocation inv) {
				return getState(inv) != null;
			}

			private MathState getState(CommandInvocation inv) {
				return getState(inv.event);
			}

			private MathState getState(MessageReceivedEvent event) {
				return getState(event.getChannel().getId());
			}

			private MathState getState(String channelId) {
				return channelToProblemMap.get(channelId);
			}

			private MessageEmbed printState(MathState state, MessageReceivedEvent event) {
				return new EmbedBuilder()
						.setAuthor("Math Lobby - " + String.format("%.2f", state.diff), null,
								event.getGuild().getIconUrl())
						.setDescription("Difficulty: `" + String.format("%.2f", state.diff) + "`\nNext Reward: **"
								+ Utilities.format(state.value) + "**\nPlayers: "
								+ JavaTools.printInEnglish(JavaTools.mask(state.players.iterator(),
										a -> event.getJDA().getUserById(a).getAsMention()), true)
								+ "\nCurrent Problem:```" + state.problem.problem() + "```\nTime Remaining: __"
								+ Utilities.formatLargest(Duration.between(Instant.now(), state.inst), 2) + "__")
						.setFooter("Use ~math leave to leave the lobby.",
								event.getJDA().getSelfUser().getEffectiveAvatarUrl())
						.build();
			}

			private MessageEmbed printStateOver(MathState state, CommandInvocation inv) {
				return new EmbedBuilder()
						.setAuthor("Math Lobby - " + String.format("%.2f", state.diff), null,
								inv.event.getGuild().getIconUrl())
						.setDescription("Difficulty: `" + String.format("%.2f", state.diff) + "`\nMissed Reward: **"
								+ Utilities.format(state.value) + "**\nPlayers: "
								+ JavaTools.printInEnglish(JavaTools.mask(state.players.iterator(),
										a -> inv.event.getJDA().getUserById(a).getAsMention()), true)
								+ "\nFinal Problem:```" + state.problem.problem() + "```\n**Correct Answer:** `"
								+ state.problem.answer() + "`\n\n**GAME OVER!**")
						.setFooter("Use ~math leave to leave the lobby.",
								inv.event.getJDA().getSelfUser().getEffectiveAvatarUrl())
						.build();
			}

			private void end(String channel) {
				var ms = getState(channel);
				if (ms == null)
					return;

				clover.getEventHandler().getMessageProcessor().removeInputConsumer(ms.mic);
				channelToProblemMap.remove(channel);
				ms.ts.cancel();
			}

			@Override
			public void exec(CommandInvocation inv) {
				var ms = getState(inv);
				if (running(inv)) {
					if (inv.args.length == 0) {
						inv.event.getChannel().sendMessage("Current math game:").embed(printState(ms, inv.event))
								.queue();
					} else if (inv.args.length == 1) {
						if (StringTools.equalsAnyIgnoreCase(inv.args[0], "clear", "stop", "quit", "leave", "exit")) {
							if (ms.players.contains(inv.event.getAuthor().getId())) {
								ms.players.remove(inv.event.getAuthor().getId());
							} else {
								inv.event.getChannel().sendMessage("You weren't in that lobby.").queue();
								return;
							}
							if (ms.players.isEmpty()) {
								inv.event.getChannel().sendMessage("The math lobby has ended!")
										.embed(printState(ms, inv.event)).queue();
								end(inv.event.getChannel().getId());
							} else {
								inv.event.getChannel()
										.sendMessage(inv.event.getAuthor().getAsMention() + " you exited the lobby.")
										.queue();
							}
						} else
							inv.event.getChannel().sendMessage(
									"Unknown argument. Use `~math leave` to leave the lobby. Once all players leave, the lobby will end.")
									.queue();
					} else {
						inv.event.getChannel().sendMessage(
								"Too many arguments. The math command only allows 1 argument when there's an active lobby.")
								.queue();
					}
				} else {
					if (inv.args.length == 0) {
						ms = new MathState();
						channelToProblemMap.put(inv.event.getChannel().getId(), ms);
						ms.players.add(inv.event.getAuthor().getId());
						ms.problem = mpg.generate(ms.diff);
						ms.inst = Instant.now().plusSeconds(30);
						var ms2 = ms;
						timer.schedule(ms.ts = new TimerTask() {

							@Override
							public void run() {
								end(inv.event.getChannel().getId());
								inv.event.getChannel()
										.sendMessage(
												"**Time's Up!** No one answered the math problem correctly in time!")
										.embed(printStateOver(ms2, inv)).queue();
							}
						}, 30000);

						ms.mic = new MessageInputConsumer() {

							@Override
							public boolean consume(MessageReceivedEvent event,
									InputProcessor<? extends MessageReceivedEvent> processor,
									InputConsumer<MessageReceivedEvent> consumer) {
								MathState state = getState(event);

								var s = state.problem.check(event.getMessage().getContentDisplay());
								if (s == AttemptResult.CORRECT) {
									if (!ms2.players.contains(event.getAuthor().getId()))
										ms2.players.add(event.getAuthor().getId());
									ms2.ts.cancel();
									timer.schedule(ms2.ts = new TimerTask() {

										@Override
										public void run() {
											end(inv.event.getChannel().getId());
											inv.event.getChannel().sendMessage(
													"**Time's Up!** No one answered the math problem correctly in time!")
													.embed(printStateOver(ms2, inv)).queue();
										}
									}, 30000);
									ms2.inst = Instant.now().plusSeconds(30);
									BigInteger amt = ms2.value;
									ms2.upgrade();
									ms2.problem = mpg.generate(ms2.diff);
									for (var p : ms2.players) {
										UserAccount acc = clover.getEconomy().getAccount(p);
										acc.deposit(amt);
										acc.save();
									}
									inv.event.getChannel().sendMessage(event.getAuthor().getAsMention()
											+ " you solved the problem! Everyone's been given " + Utilities.format(amt)
											+ " and the difficulty has been increased to `"
											+ String.format("%.2f", ms2.diff) + "`! The new problem is: ```"
											+ ms2.problem.problem() + "```").queue();
									return true;
								} else if (s == AttemptResult.INCORRECT) {
									if (!ms2.players.contains(event.getAuthor().getId()))
										ms2.players.add(event.getAuthor().getId());
									event.getMessage().addReaction("\u274C").queue();
									return true;
								} else
									return false;
							}
						}.filterChannel(inv.event.getChannel().getId());
						clover.getEventHandler().getMessageProcessor().registerInputConsumer(ms.mic);

						inv.event.getChannel().sendMessage("Starting a new math lobby!")
								.embed(printState(ms, inv.event)).queue();
					} else {
						inv.event.getChannel().sendMessage(
								"The math command doesn't take any arguments unless you're in a game! (Just use `~math` to start a new lobby.)")
								.queue();
					}
				}
			}
		});

//		help.addCommand("stats", "Shows a user's stats!", "stats [user]", "info");
		help.addCommand("tip", "Shows a random tip!", "tip");
		help.addCommand("daily", "Receive daily rewards! You can only run this once a day.", "daily");
		help.addCommand("weekly", "Receive weekly rewards! You can only run this once a day.", "weekly");
		help.addCommand("monthly", "Receive monthly rewards! You can only run this once a day.", "monthly");
		help.addCommand("open", "Open a crate or loot box, or use an item! Use this to open `crate`s or use `food`.",
				"open [item-type] (item)", "use");
		help.addCommand("mults", "Shows you what multipliers you have active.", "mults");
		help.addCommand("color", "Lets you purchase a color role.", "color ", "color-role");
		help.addCommand("pay", "Use this command to pay other people.", "pay (user) (amount)");
		help.addCommand("balance", "Tells you how rich you are.", "balance", "bal");
		help.addCommand("baltop", "Check out who the richest people in this server are!", "baltop [page]",
				"leaderboard");
		help.addCommand("inventory", "Shows your inventory.", "inventory [item-id] [page]", "inv");
		help.addCommand("accolades",
				"Shows you what accolades you have. Use a number to get info about a specific accolade you have, for example `~accolades 2` will give you information about the second accolade you have.",
				"accolades [index]");
		{
			var quizHelp = help.addParentCommand("quiz",
					"Lets you make, see, and give quizzes! (You must be a Clover Officer to access this command!)");
			quizHelp.addSubcommand("list", "Lists your questions if you have any registered.", "quiz list [page]",
					"view");
			quizHelp.addSubcommand("new", "Walks you through creating a new quiz question.",
					"quiz new (value) (difficulty)", "make", "create");
			quizHelp.addSubcommand("delete", "Use this to get rid of any of your questions.",
					"quiz delete (question-number)", "remove", "del", "rem");
		}
		// setup cmd help inside command object.
		help.addCommand("trade",
				"Starts a trade with another user. Trades let you securely exchange items, cloves, or other tradeable possessions.",
				"trade (@user)");
		help.addCommand("changelog",
				"Shows my Change Log, detailing all the updates that have happened to me over time.",
				"changelog [version | page]", "updates", "changes");
	}

	private static void multDispHelper(StringBuilder sb, List<Multiplier> mults) {
		class MultConv {
			final Multiplier mult;

			public MultConv(Multiplier mult) {
				this.mult = mult;
			}

			@Override
			public boolean equals(Object obj) {
				return obj instanceof MultConv && ((MultConv) obj).mult.getAmount().equals(mult.getAmount());
			}

			@Override
			public int hashCode() {
				return mult.getAmount().hashCode() * 31;
			}

		}
		var mm = JavaTools.frequencyMap(JavaTools.mask(mults, MultConv::new));
		if (!mm.isEmpty()) {
			for (var e : mm.entrySet())
				sb.append('(').append(e.getValue()).append("x) [**x").append(e.getKey().mult.getAmount())
						.append(e.getValue() == 1 ? "**] for " : "**] for about ")
						.append(Utilities.formatLargest(e.getKey().mult.getTimeRemaining(), 2)).append('\n');
		}

	}

}
