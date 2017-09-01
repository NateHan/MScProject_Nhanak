package models.formdata

case class AddCollaboratorData(userToAdd:String, permissionSelection:String) {

  def permissionAsInt(): Int = {
    permissionSelection match {
      case "Project Contributor: View all, add notes, upload data" => 200
      case "Project Contributor: View all, add notes, no table data upload/addition"=> 250
      case "External Viewer: View all data - no write or upload" => 300
      case "Public: View only" => 400
    }
  }
}
