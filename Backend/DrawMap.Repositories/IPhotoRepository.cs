using DrawMap.Domain;

namespace DrawMap.Repositories;

public interface IPhotoRepository
{
    Task<string> AddPhoto(Photo photo);
    Task<Photo?> GetPhoto(string photoId);
    Task<Photo?> UpdatePhoto(string photoId, Photo photo);
    Task<bool> DeletePhoto(string photoId);
    Task<List<Photo>> ListPhotosByRoute(string routeId);
}
