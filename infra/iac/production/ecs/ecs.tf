# resource "aws_ecs_cluster" "onlynotes_cluster" {
#   name = "${var.app_name}-ecs-cluster-${var.environment}"
#   tags = {}
#   tags_all = {}
#
#   service_connect_defaults {
#     namespace = "arn:aws:servicediscovery:ap-southeast-1:211125709264:namespace/ns-26byzbudmn2rui4r"
#   }
#
#   setting {
#     name  = "containerInsights"
#     value = "disabled"
#   }
#
# }
#
