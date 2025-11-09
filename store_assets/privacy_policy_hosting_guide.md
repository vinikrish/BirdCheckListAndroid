# Privacy Policy Hosting Guide

## Quick Options to Host Your Privacy Policy (Free)

### Option 1: GitHub Pages (Recommended - Free & Easy)
1. **Create a new repository** on GitHub (e.g., `birdchecklist-privacy`)
2. **Upload the `privacy_policy.html` file**
3. **Enable GitHub Pages** in repository settings
4. **Your URL will be:** `https://[username].github.io/birdchecklist-privacy/privacy_policy.html`

**Steps:**
```bash
# Create new repo on GitHub, then:
git clone https://github.com/[username]/birdchecklist-privacy.git
cd birdchecklist-privacy
cp ../BirdCheckListAndroid/store_assets/privacy_policy.html .
git add privacy_policy.html
git commit -m "Add privacy policy"
git push
# Enable Pages in repo settings → Pages → Source: Deploy from branch → main
```

### Option 2: Netlify (Free)
1. Go to [netlify.com](https://netlify.com)
2. Drag and drop the `privacy_policy.html` file
3. Get instant URL like: `https://[random-name].netlify.app/privacy_policy.html`

### Option 3: Firebase Hosting (Free)
1. Use your existing Firebase project
2. Deploy the privacy policy file
3. URL: `https://[project-id].web.app/privacy_policy.html`

### Option 4: Google Sites (Free)
1. Go to [sites.google.com](https://sites.google.com)
2. Create a new site
3. Copy-paste the privacy policy content
4. Publish and get a URL

## Before Publishing - Update Contact Information

**IMPORTANT:** Edit the privacy policy file and replace:
- `[Your Email Address]` with your actual email
- `[Your Name/Company]` with your name or company name

## For Google Play Store

Use the final URL in the "Privacy policy URL" field. Examples:
- `https://yourusername.github.io/birdchecklist-privacy/privacy_policy.html`
- `https://your-site.netlify.app/privacy_policy.html`
- `https://your-project.web.app/privacy_policy.html`

## Legal Compliance

This privacy policy template covers:
- ✅ Data collection and usage
- ✅ Children's privacy (COPPA compliance)
- ✅ User rights (GDPR basics)
- ✅ Third-party services
- ✅ Contact information
- ✅ Google Play Store requirements

**Note:** Consider consulting with a legal professional for apps with complex data handling or commercial use.