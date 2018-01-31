#import "FGAppDelegate.h"
#import "FGWKWebViewController.h"

@implementation FGAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    UITabBarController *tabBarController = [[UITabBarController alloc] init];
    
    if([WKWebView class]) {
        FGWKWebViewController* WKWebViewFGController = [[FGWKWebViewController alloc] init];
        WKWebViewFGController.tabBarItem.title             = @"WKWebView";
        [tabBarController addChildViewController:WKWebViewFGController];
    }

    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.rootViewController = tabBarController;
    [self.window makeKeyAndVisible];
    return YES;
}

@end
