//
//  FGWKWebViewController.m
//  FGApp-iOS
//
//

#import "FGWKWebViewController.h"
#import "WebViewJavascriptBridge.h"
#import <Foundation/Foundation.h>
#import "WXApiObject.h"
#import "WXApi.h"
#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApiInterface.h>

@interface FGWKWebViewController ()

@property WebViewJavascriptBridge* bridge;
@property UIButton *viewShare;
@end

@implementation FGWKWebViewController
{
    __block NSDictionary *_shareData;
}

- (void)viewWillAppear:(BOOL)animated {
    if (_bridge) { return; }
    
    WKWebView* webView = [[NSClassFromString(@"WKWebView") alloc] initWithFrame:self.view.bounds];
    webView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth|
    UIViewAutoresizingFlexibleLeftMargin|UIViewAutoresizingFlexibleRightMargin|
    UIViewAutoresizingFlexibleTopMargin|UIViewAutoresizingFlexibleBottomMargin;
    webView.navigationDelegate = self;

    [self.view addSubview:webView];
    [WebViewJavascriptBridge enableLogging];
    _bridge = [WebViewJavascriptBridge bridgeForWebView:webView];
    [_bridge setWebViewDelegate:self];
    
    [_bridge registerHandler:@"clickShare" handler:^(id data, WVJBResponseCallback responseCallback) {
        NSLog(@"testObjcCallback called: %@", data);
        _shareData = data;
        [self showShare:data];
    }];
    
    //[_bridge callHandler:@"callJavascriptHandler" data:@{ @"foo":@"before ready" }];
    
    //[self showShare:nil];
    [webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"http://123.207.136.167:8888/fgapp/login.html"]]];
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation {
    NSLog(@"webViewDidStartLoad");
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    NSLog(@"webViewDidFinishLoad");
}

- (void)showShare:(NSDictionary*)data {
    if (!self.viewShare) {
        float h = 42;
        float w = self.view.frame.size.width;
        UIButton *container = [UIButton buttonWithType:UIButtonTypeCustom];
        container.frame = self.view.bounds;
        container.backgroundColor = [UIColor clearColor];
        [container addTarget:self action:@selector(dismissShare:) forControlEvents:UIControlEventTouchUpInside];
        
        UIView *v= [[UIView alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height-h, self.view.frame.size.width, h)];
        v.backgroundColor = [UIColor whiteColor];
        
        UIButton *b = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 32, 32)];
        b.backgroundColor = [UIColor clearColor];
        [b setImage:[UIImage imageNamed:@"icon_qq"] forState:UIControlStateNormal];
        b.center = CGPointMake(w/6, h/2);
        [b addTarget:self action:@selector(callHandler:) forControlEvents:UIControlEventTouchUpInside];
        b.tag = 0;
        [v addSubview:b];
        
        b = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 32, 32)];
        b.backgroundColor = [UIColor clearColor];
        [b setImage:[UIImage imageNamed:@"ic_wx"] forState:UIControlStateNormal];
        b.center = CGPointMake(w/2, h/2);
        [b addTarget:self action:@selector(callHandler:) forControlEvents:UIControlEventTouchUpInside];
        b.tag = 1;
        [v addSubview:b];
        
        b = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 32, 32)];
        b.backgroundColor = [UIColor clearColor];
        [b setImage:[UIImage imageNamed:@"ic_wx_friends"] forState:UIControlStateNormal];
        b.center = CGPointMake(w*5/6, h/2);
        [b addTarget:self action:@selector(callHandler:) forControlEvents:UIControlEventTouchUpInside];
        b.tag = 2;
        [v addSubview:b];
        
        [container addSubview:v];
        self.viewShare = container;
    }
    [self.view addSubview:self.viewShare];
}

- (void)callHandler:(id)sender {
//    id data = @{ @"greetingFromObjC": @"Hi there, JS!" };
//    [_bridge callHandler:@"callJavascriptHandler" data:data responseCallback:^(id response) {
//        NSLog(@"testJavascriptHandler responded: %@", response);
//    }];
    
    NSString *targetUrl = [NSString stringWithFormat:@"%@", [_shareData objectForKey:@"targetUrl"]];
    NSString *title=[NSString stringWithFormat:@"%@", [_shareData objectForKey:@"title"]];
    NSString *imageUrl = [NSString stringWithFormat:@"%@", [_shareData objectForKey:@"imageUrl"]];
    NSString *desc = [NSString stringWithFormat:@"%@", [_shareData objectForKey:@"description"]];
    if ([sender tag] == 0) {
        QQApiURLObject *urlObject = [QQApiURLObject
                                     objectWithURL:[NSURL URLWithString:targetUrl]
                                     title:title
                                     description:desc
                                     previewImageURL:[NSURL URLWithString:imageUrl]
                                     targetContentType:QQApiURLTargetTypeNews];
        SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:urlObject];
        // 分享给好友
        [QQApiInterface sendReq:req];
    }else{
        UIImage *thumbImage = [UIImage imageNamed:@"logo.png"];
        WXWebpageObject *ext = [WXWebpageObject object];
        ext.webpageUrl = targetUrl;
        
        WXMediaMessage *message = [WXMediaMessage message];
        message.title = title;
        message.description = desc;
        [message setThumbImage:thumbImage];
        message.mediaObject = ext;
        message.mediaTagName = [[NSDate date] description];
        
        SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];
        req.message = message;
        req.scene = [sender tag] == 1 ? WXSceneSession : WXSceneTimeline;
        [WXApi sendReq:req];
    }
}

- (void)dismissShare:(id)w {
    [self.viewShare removeFromSuperview];
}

@end
