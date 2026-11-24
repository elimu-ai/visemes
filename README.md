# Visemes 👄

Android application which animates visemes

A viseme is the mouth shape that goes with a speech sound. The app loads all letter-sound correspondences from the [elimu.ai Content Provider](https://github.com/elimu-ai/content-provider), picks the most frequently used one, and shows its sound(s) together with an animated cartoon mouth (purple lips, teeth and tongue). Press the mouth, or the button, to hear the sound: while it plays, the mouth moves from neutral to the shape of the sound, and back to neutral.

Built with Jetpack Compose.

| Part | Where |
| --- | --- |
| Screen showing the most frequent letter-sound | `ui/LetterSoundVisemeActivity.kt` |
| `@Composable` lips, drawn on a `Canvas` | `ui/lips/Lips.kt` |
| Neutral → sound → neutral animation | `ui/lips/AnimatedLips.kt` |
| Mouth shape of each IPA sound | `viseme/Viseme.kt`, `viseme/VisemeMapper.kt` |

**Requires** the elimu.ai Content Provider app to be installed on the device.

---

<p align="center">
  <img src="https://github.com/elimu-ai/webapp/blob/main/src/main/webapp/static/img/logo-text-256x78.png" />
</p>
<p align="center">
  elimu.ai - Free open-source learning software for out-of-school children ✨🚀
</p>
<p align="center">
  <a href="https://elimu.ai">Website 🌐</a>
  &nbsp;•&nbsp;
  <a href="https://github.com/elimu-ai/wiki#readme">Wiki 📃</a>
  &nbsp;•&nbsp;
  <a href="https://github.com/orgs/elimu-ai/projects?query=is%3Aopen">Projects 👩🏽‍💻</a>
  &nbsp;•&nbsp;
  <a href="https://github.com/elimu-ai/wiki/milestones">Milestones 🎯</a>
  &nbsp;•&nbsp;
  <a href="https://github.com/elimu-ai/wiki#open-source-community">Community 👋🏽</a>
  &nbsp;•&nbsp;
  <a href="https://www.drips.network/app/drip-lists/41305178594442616889778610143373288091511468151140966646158126636698">Support 💜</a>
</p>
