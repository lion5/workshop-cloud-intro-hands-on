variable "map_users" {
  description = "Additional IAM users to add to the aws-auth configmap."
  type = list(object({
    userarn  = string
    username = string
    groups   = list(string)
  }))

  default = [
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtrainer"
      username  = "cloudtrainer"
      groups    = ["system:masters"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining01"
      username  = "cloudtraining01"
      groups    = ["group01-full-right-binding"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining02"
      username  = "cloudtraining02"
      groups    = ["group02-full-right-binding"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining03"
      username  = "cloudtraining03"
      groups    = ["group03-full-right-binding"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining04"
      username  = "cloudtraining04"
      groups    = ["group04-full-right-binding"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining05"
      username  = "cloudtraining05"
      groups    = ["group05-full-right-binding"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining06"
      username  = "cloudtraining06"
      groups    = ["group06-full-right-binding"]
    },
    { 
      userarn   = "arn:aws:iam::935581097791:user/cloudtraining07"
      username  = "cloudtraining07"
      groups    = ["group07-full-right-binding"]
    },
  ]
}