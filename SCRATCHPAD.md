# DONE
- the download indicator is meant to be displayed in the sidebar to be always showing whats going on.
    - that means when theres nothing in progress, it is not shown.
	- when there is a downlaod / install process in progress it should be shown with the appropriate info
	- when that process is done it should stay shown until the user clicks the cross button to dismiss it
	- it should not show in the app detail view, instead the appdetail view should have its own simplified version of it, which should blend in more with the layout and structure of the app detail view.
- the terminal output is getting spammed with this message:
  "Dec 16, 2025 3:27:17 PM com.example.appstore.components.InstallationIndicator <init>
INFO: [InstallationIndicator] Created for app: upscayl, step: DOWNLOADING, status: PROCESSING"
this should be fixed by only printing messages like this once, in this case only when the downloading step began, not while its running
- theres a weird behaviour of the appdetail view which will expand extensivly in width due to images having very wide aspect ratios and the view adapts to that for no good reason. the images shown in the screenshot view should not be wider than the page itself. please fix that.

# PROBLEMS
- theres a problem with the display of images somehow, the urls from the readmes are valid pngs and svgs, i checked the logs on debug level but only some of them are shown and its not just the pngs, because sometimes they are not shown and the svgs are.
- apps on macos should be installed in Applications, but at the top level, not in subdirs like with wezterm for example

- better error handeling when installation fails
- quick jump link to get to repos lol

# KNOW APPS THAT FAIL TO INSTALL CURRENTLY

## upscayl
Dec 16, 2025 3:44:15 PM com.example.appstore.views.AppDetailView lambda$new$3
INFO: [AppDetailView] Install button clicked for: upscayl
Dec 16, 2025 3:44:15 PM com.example.appstore.views.AppDetailView lambda$new$3
INFO: [AppDetailView] Starting installation for: upscayl
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationManager installApp
INFO: [InstallationManager] Starting installation for: upscayl
Dec 16, 2025 3:44:15 PM com.example.appstore.components.Sidebar updateIndicator
INFO: [Sidebar] Creating indicator for: upscayl
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService <init>
INFO: [InstallationService] Initialized
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService installApp
INFO: [InstallationService] Starting installation for: upscayl (id: upscayl)
Dec 16, 2025 3:44:15 PM com.example.appstore.service.PlatformDetector detectPlatform
INFO: [PlatformDetector] Detecting platform - OS: mac os x, Arch: aarch64
Dec 16, 2025 3:44:15 PM com.example.appstore.service.PlatformDetector detectPlatform
INFO: [PlatformDetector] Detected platform: macOS
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Detected platform: macOS, arch: arm64
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Stage 1: Fetching release info for upscayl
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Found release: v2.15.0 (2.15 New Year Update)
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Release has 14 assets:
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Selected asset: upscayl-2.15.0-mac.dmg (367.4 MB)
Dec 16, 2025 3:44:15 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Stage 2: Downloading upscayl-2.15.0-mac.dmg
Dec 16, 2025 3:44:27 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Download completed in 12506ms, saved to: /Users/noah/.stars/downloads/upscayl-2.15.0-mac.dmg
Dec 16, 2025 3:44:27 PM com.example.appstore.service.InstallationService doInstall
INFO: [InstallationService] Stage 3: Installing from upscayl-2.15.0-mac.dmg
Dec 16, 2025 3:44:27 PM com.example.appstore.service.InstallationService lambda$installApp$0
SEVERE: [InstallationService] Installation failed for: upscayl
java.lang.RuntimeException: Failed to mount DMG
        at com.example.appstore.service.InstallationService.installDMG(InstallationService.java:380)
        at com.example.appstore.service.InstallationService.installMacOS(InstallationService.java:334)
        at com.example.appstore.service.InstallationService.installAsset(InstallationService.java:315)
        at com.example.appstore.service.InstallationService.doInstall(InstallationService.java:209)
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:128)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1789)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1781)
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)
        at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)

Dec 16, 2025 3:44:27 PM com.example.appstore.service.InstallationManager lambda$installApp$1
SEVERE: [InstallationManager] Installation failed for: upscayl
java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: Failed to mount DMG
        at java.base/java.util.concurrent.CompletableFuture.wrapInCompletionException(CompletableFuture.java:323)
        at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:359)
        at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:364)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1791)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1781)
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)
        at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)
Caused by: java.lang.RuntimeException: java.lang.RuntimeException: Failed to mount DMG
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:136)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1789)
        ... 5 more
Caused by: java.lang.RuntimeException: Failed to mount DMG
        at com.example.appstore.service.InstallationService.installDMG(InstallationService.java:380)
        at com.example.appstore.service.InstallationService.installMacOS(InstallationService.java:334)
        at com.example.appstore.service.InstallationService.installAsset(InstallationService.java:315)
        at com.example.appstore.service.InstallationService.doInstall(InstallationService.java:209)
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:128)
        ... 6 more

Dec 16, 2025 3:44:27 PM com.example.appstore.components.InstallationIndicator fail
WARNING: [InstallationIndicator] Failed: upscayl - java.lang.RuntimeException: Failed to mount DMG
Dec 16, 2025 3:44:27 PM com.example.appstore.views.AppDetailView lambda$new$10
SEVERE: [AppDetailView] Installation error
java.util.concurrent.CompletionException: java.lang.RuntimeException: java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: Failed to mount DMG
        at java.base/java.util.concurrent.CompletableFuture.wrapInCompletionException(CompletableFuture.java:323)
        at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:359)
        at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:364)
        at java.base/java.util.concurrent.CompletableFuture.uniExceptionally(CompletableFuture.java:1015)
        at java.base/java.util.concurrent.CompletableFuture$UniExceptionally.tryFire(CompletableFuture.java:995)
        at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:531)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1794)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1781)
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)
        at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)
Caused by: java.lang.RuntimeException: java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: Failed to mount DMG
        at com.example.appstore.service.InstallationManager.lambda$installApp$1(InstallationManager.java:218)
        at java.base/java.util.concurrent.CompletableFuture.uniExceptionally(CompletableFuture.java:1011)
        ... 8 more
Caused by: java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: Failed to mount DMG
        at java.base/java.util.concurrent.CompletableFuture.wrapInCompletionException(CompletableFuture.java:323)
        at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:359)
        at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:364)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1791)
        ... 5 more
Caused by: java.lang.RuntimeException: java.lang.RuntimeException: Failed to mount DMG
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:136)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1789)
        ... 5 more
Caused by: java.lang.RuntimeException: Failed to mount DMG
        at com.example.appstore.service.InstallationService.installDMG(InstallationService.java:380)
        at com.example.appstore.service.InstallationService.installMacOS(InstallationService.java:334)
        at com.example.appstore.service.InstallationService.installAsset(InstallationService.java:315)
        at com.example.appstore.service.InstallationService.doInstall(InstallationService.java:209)
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:128)
        ... 6 more

## rio
INFO: [AppDetailView] Install button clicked for: rio
Dec 16, 2025 4:33:51 PM com.example.appstore.views.AppDetailView lambda$new$3          INFO: [AppDetailView] Starting installation for: rio
Dec 16, 2025 4:33:51 PM com.example.appstore.service.InstallationManager installApp    INFO: [InstallationManager] Starting installation for: rio
Dec 16, 2025 4:33:51 PM com.example.appstore.components.Sidebar updateIndicator
INFO: [Sidebar] Creating indicator for: rio
Dec 16, 2025 4:33:51 PM com.example.appstore.service.InstallationService <init>
INFO: [InstallationService] Initialized
Dec 16, 2025 4:33:51 PM com.example.appstore.service.InstallationService installApp    INFO: [InstallationService] Starting installation for: rio (id: rio)
Dec 16, 2025 4:33:51 PM com.example.appstore.service.PlatformDetector detectPlatform   INFO: [PlatformDetector] Detecting platform - OS: mac os x, Arch: aarch64
Dec 16, 2025 4:33:51 PM com.example.appstore.service.PlatformDetector detectPlatform   INFO: [PlatformDetector] Detected platform: macOS
Dec 16, 2025 4:33:51 PM com.example.appstore.service.InstallationService doInstall     INFO: [InstallationService] Detected platform: macOS, arch: arm64
Dec 16, 2025 4:33:51 PM com.example.appstore.service.InstallationService doInstall     INFO: [InstallationService] Stage 1: Fetching release info for rio
Dec 16, 2025 4:33:52 PM com.example.appstore.service.InstallationService doInstall     INFO: [InstallationService] Found release: v0.2.36 (v0.2.36)                           Dec 16, 2025 4:33:52 PM com.example.appstore.service.InstallationService doInstall     INFO: [InstallationService] Release has 14 assets:
Dec 16, 2025 4:33:52 PM com.example.appstore.service.InstallationService doInstall     WARNING: [InstallationService] No compatible asset found for platform: MACOS
Dec 16, 2025 4:33:52 PM com.example.appstore.service.InstallationService lambda$installApp$0                                                                                  SEVERE: [InstallationService] Installation failed for: rio                             java.lang.RuntimeException: No compatible download found for macOS                             at com.example.appstore.service.InstallationService.doInstall(InstallationService.java:179)
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:128)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1789)                                                                              at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1781)                                                                             at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)           at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)                                                                                 at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)

Dec 16, 2025 4:33:52 PM com.example.appstore.service.InstallationManager lambda$installApp$1                                                                                  SEVERE: [InstallationManager] Installation failed for: rio
java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: No compatible download found for macOS
        at java.base/java.util.concurrent.CompletableFuture.wrapInCompletionException(CompletableFuture.java:323)
        at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:359)
        at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:364)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1791)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1781)                                                                             at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)           at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)
Caused by: java.lang.RuntimeException: java.lang.RuntimeException: No compatible download found for macOS
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:136)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1789)
        ... 5 more
Caused by: java.lang.RuntimeException: No compatible download found for macOS                  at com.example.appstore.service.InstallationService.doInstall(InstallationService.java:179)
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:128)
        ... 6 more
Dec 16, 2025 4:33:52 PM com.example.appstore.components.InstallationIndicator fail
WARNING: [InstallationIndicator] Failed: rio - java.lang.RuntimeException: No compatible download found for macOS
Dec 16, 2025 4:33:52 PM com.example.appstore.views.AppDetailView lambda$new$10
SEVERE: [AppDetailView] Installation error
java.util.concurrent.CompletionException: java.lang.RuntimeException: java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: No compatible download found for macOS
        at java.base/java.util.concurrent.CompletableFuture.wrapInCompletionException(CompletableFuture.java:323)                                                                     at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:359)
        at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:364)
        at java.base/java.util.concurrent.CompletableFuture.uniExceptionally(CompletableFuture.java:1015)
        at java.base/java.util.concurrent.CompletableFuture$UniExceptionally.tryFire(CompletableFuture.java:995)
        at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:531)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1794)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1781)
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)
        at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1450)
        at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:2019)
        at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:187)                                                                             Caused by: java.lang.RuntimeException: java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: No compatible download found for macOS                                                                                              at com.example.appstore.service.InstallationManager.lambda$installApp$1(InstallationManager.java:218)
        at java.base/java.util.concurrent.CompletableFuture.uniExceptionally(CompletableFuture.java:1011)
        ... 8 more
Caused by: java.util.concurrent.CompletionException: java.lang.RuntimeException: java.lang.RuntimeException: No compatible download found for macOS
        at java.base/java.util.concurrent.CompletableFuture.wrapInCompletionException(CompletableFuture.java:323)
        at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:359)
        at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:364)
        at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1791)                                                                              ... 5 more
Caused by: java.lang.RuntimeException: java.lang.RuntimeException: No compatible download found for macOS
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:136)                                                                         at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1789)
        ... 5 more
Caused by: java.lang.RuntimeException: No compatible download found for macOS                  at com.example.appstore.service.InstallationService.doInstall(InstallationService.java:179)
        at com.example.appstore.service.InstallationService.lambda$installApp$0(InstallationService.java:128)
        ... 6 more


# OTHER
- the backend needs caching asap, for the release assets as well as for the images and readmes
- lets throw rig.rs in there for suggesting apps based on users needs and also for something else maybe
- maybe let the backend check if theres an asset for the clients current platform?