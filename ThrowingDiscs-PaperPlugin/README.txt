========================================
  ThrowingDiscs - Paper Plugin (1.21+)
========================================

Is ZIP mein poora Paper/Purpur plugin source code hai jo Throwing Discs Fabric mod ko bina client mod ke chalata hai.

--- JAR KAISE BANAYEIN (2 EASY TAREEKE) ---

TAREEKA 1: Bina kuch install kiye (GitHub Actions se - 2 minute)
1. GitHub.com par ek naya repository banao (Private ya Public).
2. Is ZIP ko extract karo aur saari files (`.github` folder samet) us repo mein upload kar do.
3. Repo ke "Actions" tab par jao -> "Build ThrowingDiscs Plugin" automatically run hoga.
4. Build green hone par neeche "Artifacts" mein se `ThrowingDiscs-Paper-JAR` download karo.
5. Us `.jar` file ko apne server ke `plugins/` folder mein daal do!

TAREEKA 2: Apne PC par (Maven se)
1. Java 21 (JDK 21) aur Apache Maven install karo.
2. Is folder mein terminal/CMD kholo aur run karo:
   mvn clean package
3. Ready plugin file yahan milegi:
   target/ThrowingDiscs-Paper-1.2.0.jar

--- RESOURCE PACK SETUP ---
- Sath mein diye gaye `ThrowingDiscs-ResourcePack.zip` ko apne server ke `server.properties` mein `resource-pack=` par lagao (ya players ko de do).
- Isse sabhi 8 discs (Copper, Iron, Gold, Diamond, Netherite, Redstone, God, Frisbee) ke exact textures dikhenge.

--- COMMANDS ---
/td list                     -> Sabhi discs ke naam dekho
/td get <disc>               -> Apne aap ko disc do (e.g. /td get diamond)
/td give <player> <disc>     -> Kisi player ko disc do
