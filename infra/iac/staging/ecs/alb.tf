resource "aws_lb" "onlynotes-alb" {
  client_keep_alive    = 3600
  idle_timeout         = 60
  internal             = false
  ip_address_type      = "ipv4"
  load_balancer_type   = "application"
  name                 = "${var.app_name}-ecs-lb-${var.environment}"
  name_prefix          = null
  preserve_host_header = false
  security_groups = [
    "sg-04e5c3d61d409c3b6"
  ]
  subnets = [
    "subnet-0161ca9ff7e35d928",
    "subnet-01c958fe22a72a561",
    "subnet-07da9d2924f17ec9c"
  ]
  tags = {}
  tags_all = {}

}

resource "aws_acm_certificate" "onlynotes_lb_cert" {
  domain_name   = "apis.onlynotes.net"
  key_algorithm = "RSA_2048"
  subject_alternative_names = [
    "apis.onlynotes.net",
  ]
  options {
    certificate_transparency_logging_preference = "ENABLED"
  }
}


resource "aws_lb_listener" "onlynotes_lb_listener" {
  load_balancer_arn = aws_lb.onlynotes-alb.arn
  certificate_arn   = aws_acm_certificate.onlynotes_lb_cert.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  tags = {}
  tags_all = {}

  default_action {
    order = 1
    type  = "forward"

    forward {
      stickiness {
        duration = 3600
        enabled  = false
      }
      target_group {
        arn    = "arn:aws:elasticloadbalancing:ap-southeast-1:211125709264:targetgroup/temp/9db0d7f51b6192b8"
        weight = 1
      }
    }
  }

  mutual_authentication {
    ignore_client_certificate_expiry = false
    mode                             = "off"
    trust_store_arn                  = null
  }
}
