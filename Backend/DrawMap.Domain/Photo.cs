namespace DrawMap.Domain;

public class Photo
{
    public string? Id { get; set; }
    public string? RouteId { get; set; }
    public Location Location { get; set; }
    public byte[]? Data { get; set; }
}