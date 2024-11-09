
module "cognito" {
  source = "./cognito"
  arn-acm_cognito-domain = var.arn-acm_cognito-domain
  environment = var.environment
  cognito_callback_url = var.cognito_callback_url
  cognito_logout_url = var.cognito_logout_url
  cognito_domain = var.cognito_domain
  cognito_subdomain = var.cognito_subdomain
  only-notes_hosted-zone = var.only-notes_hosted-zone
}

