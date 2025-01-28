#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>
#import "RCTBridge.h"

@interface VideoMpvViewManager : RCTViewManager
@end

@implementation VideoMpvViewManager

RCT_EXPORT_MODULE(VideoMpvView)

- (UIView *)view
{
  return [[UIView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(color, NSString)

@end
