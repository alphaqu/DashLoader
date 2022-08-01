package dev.quantumfusion.dashloader.client;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.config.DashConfig;
import java.util.ArrayList;
import java.util.List;

public class HahaManager {
	private static final String[] FACTS = {
			"Did you know DashLoader was called Dash in the ancient days",
			"fun fact: 1 + 1 = 11",
			"glisco goes around and yells",
			":froge:",
			":bigfroge:",
			":smolfroge:",
			"Frog + Doge = Froge",
			"Froges dad is closest to Chuck Norris.",
			"Rogger Rogger!",
			"Yes commander!",
			"I am not the swarm!",
			"Get that golden strawberry!",
			"Kevin is cool.",
			"B-Sides are where I flex.",
			"We want to start an accelerated backhop.",
			"Gordon Freeman. I like your tie.",
			"The factory must grow.",
			"Not the biters.",
			"Ya got more red belts?",
			"Mom: go to sleep, me: I need more boilers.",
			"Throughput of circuits is good enough.",
			"amogus",
			"sus",
			"imposter",
			"it was red!",
			"What does the vent button do?",
			"We need more white wine.",
			"I season my cuttingboard not the meat.",
			"Do as I say, not as I do",
			"Colton is fired",
			"Was a banger on the cord.",
			"My code thinks different.",
			"Make it for 300$ sell it for 1300$",
			"Steve is almost as chad as Chuck Norris",
			"IKEA is traditional.",
			"1 + 1 = 11",
			"https://ko-fi.com/notequalalpha",
			"USB-C is gud.",
			"Modrinth gud.",
			"Leocth and Alpha were first.",
			"Corn on a jakob is the best.",
			"Cornebb is cool.",
			"Hyphen is cool.",
			"DashLoader kinda banger.",
			"Minecraft fact of the day was a thing.",
			":tnypotat:",
			"418 I'm a teapot is a real error",
			"mld hrdr - leocth 2022",
			// HiItsDevin
			"Devin beat 7C after 5 1/2 hours",
			// shedaniel
			"Look at me, I am vibing up here",
			"Doesn't break REI"
	};

	public static String getFact() {
		DashConfig config = DashLoader.DL.config.config;
		List<String> splashLines = new ArrayList<>(config.customSplashLines);
		if (config.addDefaultSplashLines) {
			splashLines.addAll(List.of(FACTS));
		}

		if (splashLines.isEmpty()) {
			return "im empty over here!";
		}


		return splashLines.get((int) (System.currentTimeMillis() % splashLines.size()));
	}
}
