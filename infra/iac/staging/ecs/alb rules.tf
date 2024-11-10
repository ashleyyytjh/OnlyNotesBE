resource "aws_lb_listener_rule" "request_rule" {

  listener_arn = aws_lb_listener.onlynotes_lb_listener.arn
  priority     = 1000
  tags = {}
  tags_all = {}

  action {
    type = "forward"

    forward {
      stickiness {
        duration = 3600
        enabled  = false
      }
      target_group {
        arn    = aws_lb_target_group.request_tg.arn
        weight = 1
      }
    }
  }

  condition {
    path_pattern {
      values = [
        "/api/v1/requests*",
      ]
    }
  }
}


resource "aws_lb_listener_rule" "account_rule" {
  listener_arn = aws_lb_listener.onlynotes_lb_listener.arn
  priority     = 1002
  tags = {}
  tags_all = {}

  action {
    type = "forward"

    forward {
      stickiness {
        duration = 3600
        enabled  = false
      }
      target_group {
        arn    = aws_lb_target_group.account_tg.arn
        weight = 1
      }
    }
  }

  condition {
    path_pattern {
      values = [
        "/api/v1/auth*",
        "/api/v1/auth/*",
        "/api/v1/users*",
      ]
    }
  }
}


resource "aws_lb_listener_rule" "notes_rule" {
  listener_arn = aws_lb_listener.onlynotes_lb_listener.arn
  priority     = 1003
  tags = {}
  tags_all = {}

  action {
    type = "forward"

    forward {
      stickiness {
        duration = 3600
        enabled  = false
      }
      target_group {
        arn    = aws_lb_target_group.notes_tg.arn
        weight = 1
      }
    }
  }

  condition {
    path_pattern {
      values = [
        "/api/v1/notes*",
      ]
    }
  }
}


resource "aws_lb_listener_rule" "orders_rule" {
  listener_arn = aws_lb_listener.onlynotes_lb_listener.arn
  priority     = 1004
  tags = {}
  tags_all = {}

  action {
    type = "forward"

    forward {
      stickiness {
        duration = 3600
        enabled  = false
      }
      target_group {
        arn    = aws_lb_target_group.orders_tg.arn
        weight = 1
      }
    }
  }

  condition {
    path_pattern {
      values = [
        "/api/v1/orders*",
        "/api/v1/stripe/webhook",
      ]
    }
  }
}


resource "aws_lb_listener_rule" "verify_rule" {
  listener_arn = aws_lb_listener.onlynotes_lb_listener.arn
  priority     = 1005
  tags = {}
  tags_all = {}

  action {
    type = "forward"

    forward {
      stickiness {
        duration = 3600
        enabled  = false
      }
      target_group {
        arn    = aws_lb_target_group.verify_tg.arn
        weight = 1
      }
    }
  }

  condition {
    path_pattern {
      values = [
        "/api/v1/verify*",
      ]
    }
  }
}
