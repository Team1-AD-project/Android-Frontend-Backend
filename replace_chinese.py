#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Batch replace Chinese text in Android layout files
"""

import os
import re
from pathlib import Path

# Define replacement mapping
replacements = {
    # Fragment challenge_detail.xml
    "挑战详情": "Challenge Details",
    "个人挑战": "Individual Challenge",
    "本周绿色出行挑战": "Weekly Green Transport Challenge",
    "本周完成10次绿色出行，冲击环保榜首！": "Complete 10 green trips this week and top the eco leaderboard!",
    "+500 积分": "+500 Points",
    "奖励": "Reward",
    "解锁徽章": "Unlock Badge",
    "成就": "Achievement",
    "342 人": "342 people",
    "参与": "Participate",
    "截止时间：": "Deadline:",
    "我的进度": "My Progress",
    "继续加油！你已经完成了大部分啦！": "Keep going! You've completed most of it!",
    "排行榜": "Leaderboard",
    "暂无排行榜数据": "No leaderboard data",
    "接受挑战": "Accept Challenge",
    
    # Fragment item_detail.xml
    "商品详情": "Item Details",
    "试穿预览": "Try On Preview",
    "头饰": "Headwear",
    "毕业帽": "Grad Cap",
    "价格：": "Price:",
    "200 积分": "200 Points",
    "商品说明": "Description",
    "装备此物品后，你的小狮子将会变得更加与众不同！在个人资料页面可以随时更换装备。": "Equip this item to make your little lion more unique! You can change equipment anytime on your profile page.",
    "购买": "Buy",
    "装备": "Equip",
    
    # Fragment share_impact.xml
    "分享成就": "Share Achievement",
    "今日": "Today",
    "本周": "This Week",
    "本月": "This Month",
    "我的绿色出行": "My Green Trips",
    "今日影响力": "Today's Impact",
    "行程": "Trips",
    "公里": "Kilometers",
    "积分": "Points",
    "🌱 EcoGo - 一起绿色出行": "🌱 EcoGo - Green Transport Together",
    "环保贡献": "Eco Contribution",
    "相当于种植 2 棵树\n减少 1.2 小时汽车行驶排放\n节省 $5.50 交通费用": "Equivalent to planting 2 trees\nReducing 1.2 hours of car emissions\nSaving $5.50 in transport costs",
    "保存图片": "Save Image",
    "立即分享": "Share Now",
    
    # Fragment voucher_detail.xml
    "兑换券详情": "Voucher Details",
    "券码": "Code",
    "有效期至：2026/03/31": "Valid until: 2026/03/31",
    "500 积分": "500 Points",
    "兑换所需": "Required to Redeem",
    "使用说明": "Instructions",
    "使用说明将在这里显示": "Usage instructions will be displayed here",
    "立即兑换": "Redeem Now",
    "立即使用": "Use Now",
    
    # Fragment challenges.xml
    "挑战": "Challenges",
    "全部": "All",
    "进行中": "Ongoing",
    "已完成": "Completed",
    "暂无挑战": "No Challenges",
    "稍后会有新的挑战发布": "New challenges will be released later",
    
    # Item challenge.xml
    "个人": "Individual",
    "已完成": "Completed",
    "+500 积分": "+500 Points",
    "342 人参与": "342 participants",
    
    # Fragment activity_detail.xml
    "活动详情": "Activity Details",
    "线上活动": "Online Activity",
    "进行中": "Ongoing",
    "校园环保周挑战": "Campus Eco Week Challenge",
    "参与本周的环保挑战，完成每日绿色出行任务，赢取丰厚奖励！": "Join this week's eco challenge, complete daily green transport tasks, and win great rewards!",
    "完成奖励：": "Completion Reward:",
    "+200 积分": "+200 Points",
    "参与人数：": "Participants:",
    "活动时间": "Activity Time",
    "开始时间：": "Start Time:",
    "结束时间：": "End Time:",
    "参加活动": "Join Activity",
    "开始路线": "Start Route",
    "签到": "Check In",
    
    # Trip fragments
    "行程完成！": "Trip Completed!",
    "干得漂亮！": "Well done!",
    "环保等级": "Eco Level",
    "分钟": "Minutes",
    "g CO₂ 减排": "g CO₂ Saved",
    "查看排行": "View Leaderboard",
    "兑换奖励": "Redeem Rewards",
    "再来一次": "Go Again",
    
    "前往": "To",
    "已完成": "Completed",
    "剩余": "Remaining",
    "继续加油！": "Keep going!",
    "下一步": "Next Step",
    "取消": "Cancel",
    "结束行程": "End Trip",
    
    "准备出发": "Ready to Go",
    "让我们一起出发吧！": "Let's go together!",
    "最环保": "Most Eco",
    "步行路线": "Walking Route",
    "预计信息": "Estimated Info",
    "15 分钟": "15 Minutes",
    "预计时间": "Estimated Time",
    "1.2 公里": "1.2 Kilometers",
    "预计距离": "Estimated Distance",
    "减少排放": "Reduce Emissions",
    "+120 积分": "+120 Points",
    "可获积分": "Points Available",
    "开始行程": "Start Trip",
    
    # Route options
    "🌿 最环保": "🌿 Most Eco",
    "🚶 步行": "🚶 Walk",
    "15分钟": "15 mins",
    "+50积分": "+50 points",
    "节省$2": "Save $2",
    
    # Bus card
    "前往 UTown": "To UTown",
    "2分钟": "2 mins",
    "低拥挤度": "Low Crowding",
    "⚡ 即将到达": "⚡ Arriving",
    
    # Route step
    "向东北方向前进": "Head northeast",
    "200m • 3分钟": "200m • 3 mins",
}

def replace_in_file(file_path):
    """Replace Chinese text in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        modified = False
        
        for chinese, english in replacements.items():
            if chinese in content:
                content = content.replace(chinese, english)
                modified = True
        
        if modified:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Updated: {file_path}")
            return True
        
        return False
    except Exception as e:
        print(f"✗ Error processing {file_path}: {e}")
        return False

def main():
    """Main function"""
    layout_dir = Path(r"c:\Users\csls\Desktop\ad-ui\android-app\app\src\main\res\layout")
    
    if not layout_dir.exists():
        print(f"Error: Directory not found: {layout_dir}")
        return
    
    print("Starting batch replacement...")
    print(f"Target directory: {layout_dir}\n")
    
    updated_count = 0
    for xml_file in layout_dir.glob("*.xml"):
        if replace_in_file(xml_file):
            updated_count += 1
    
    print(f"\n✓ Completed! Updated {updated_count} files.")

if __name__ == "__main__":
    main()
