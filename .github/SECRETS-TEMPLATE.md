# GitHub Secrets Configuration Template

## How to Add Secrets

1. Go to your GitHub repository
2. Navigate to: **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. Add each secret below with the corresponding value

## Required Secrets

### AWS Credentials
```
Name: AWS_ACCESS_KEY_ID
Value: <Your AWS Access Key ID>
Description: AWS IAM access key for Terraform and ECS deployment
```

```
Name: AWS_SECRET_ACCESS_KEY
Value: <Your AWS Secret Access Key>
Description: AWS IAM secret key for Terraform and ECS deployment
```

### SonarQube Configuration
```
Name: SONAR_HOST_URL
Value: https://sonarcloud.io (or your SonarQube instance URL)
Description: SonarQube/SonarCloud instance URL
```

```
Name: SONAR_TOKEN
Value: <Your SonarQube Token>
Description: SonarQube authentication token
How to get: Login to SonarQube → My Account → Security → Generate Token
```

### MongoDB
```
Name: MONGODB_URI
Value: mongodb+srv://username:password@cluster.mongodb.net/EcoGo
Description: MongoDB connection string
Format: mongodb+srv://[username]:[password]@[host]/[database]
```

### Docker Registry (GitHub Container Registry)
```
Name: DOCKER_USERNAME
Value: <Your GitHub username>
Description: GitHub username for GHCR authentication
```

```
Name: DOCKER_PASSWORD
Value: <Your GitHub Personal Access Token>
Description: GitHub PAT with packages:write permission
How to get: Settings → Developer settings → Personal access tokens → Generate new token (classic)
Scopes needed: write:packages, read:packages
```

## Optional Secrets

### Environment URLs (for Smoke Tests)

**Note:** These should be configured as repository secrets, not environment URLs.

```
Name: STAGING_URL
Value: https://staging.ecogo.example.com
Description: Staging environment URL for smoke tests
Location: Settings → Secrets and variables → Actions → New repository secret
```

```
Name: PRODUCTION_URL
Value: https://ecogo.example.com
Description: Production environment URL for smoke tests
Location: Settings → Secrets and variables → Actions → New repository secret
```

**To configure GitHub Environments:**
1. Go to Settings → Environments
2. Create "staging" environment (no protection rules needed)
3. Create "production" environment (enable "Required reviewers" for manual approval)
4. Optionally set environment-specific secrets within each environment

### Slack Notifications (if using)
```
Name: SLACK_WEBHOOK_URL
Value: <Your Slack Webhook URL>
Description: Slack webhook for pipeline notifications
```

### Email Notifications (if using)
```
Name: NOTIFICATION_EMAIL
Value: team@example.com
Description: Email for pipeline notifications
```

## Environment Variables (No Secret Required)

These are configured in the workflow file and don't need to be added as secrets:

- `REGISTRY`: ghcr.io (GitHub Container Registry)
- `IMAGE_NAME`: ${{ github.repository }}
- `SONAR_PROJECT_KEY`: ecogo

## Verification Checklist

After adding all secrets, verify:

- [ ] AWS credentials are valid (test with: `aws sts get-caller-identity`)
- [ ] SonarQube token works (test on SonarQube dashboard)
- [ ] MongoDB URI is accessible (test connection)
- [ ] Docker credentials can push to GHCR (test with `docker login ghcr.io`)

## Security Best Practices

1. **Never commit secrets to git** - Always use GitHub Secrets
2. **Rotate secrets regularly** - Update tokens every 90 days
3. **Use least privilege** - AWS IAM users should have minimal permissions
4. **Monitor secret usage** - Check GitHub Actions logs for unauthorized access
5. **Use separate credentials per environment** - Different secrets for staging/production

## Troubleshooting

### "Secret not found" error
- Ensure secret name matches exactly (case-sensitive)
- Secret must be in repository secrets, not environment secrets

### AWS authentication fails
- Verify IAM permissions include: ECS, ECR, EC2, VPC, IAM (for Terraform)
- Check access key is active in AWS Console

### SonarQube connection fails
- Verify SONAR_HOST_URL includes protocol (https://)
- Ensure token has "Execute Analysis" permission

### MongoDB connection fails
- Check URI format is correct
- Verify network access (IP whitelist in MongoDB Atlas)
- Ensure credentials are URL-encoded if they contain special characters
