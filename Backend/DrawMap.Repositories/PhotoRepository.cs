using DrawMap.DataSources;
using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;

namespace DrawMap.Repositories;

public class PhotoRepository : IPhotoRepository
{
    private readonly DrawMapDbContext _context;

    public PhotoRepository(DrawMapDbContext context)
    {
        _context = context;
    }

    public async Task<string> AddPhoto(Photo photo)
    {
        _context.Photos.Add(photo);
        await _context.SaveChangesAsync();
        return photo.Id ?? string.Empty;
    }

    public async Task<Photo?> GetPhoto(string photoId)
    {
        return await _context.Photos.FindAsync(photoId);
    }

    public async Task<Photo?> UpdatePhoto(string photoId, Photo photo)
    {
        var existing = await _context.Photos.FindAsync(photoId);
        if (existing is null)
            return null;

        existing.RouteId = photo.RouteId;
        existing.LocationId = photo.LocationId;

        await _context.SaveChangesAsync();
        return existing;
    }

    public async Task<bool> DeletePhoto(string photoId)
    {
        var existing = await _context.Photos.FindAsync(photoId);
        if (existing is null)
            return false;

        _context.Photos.Remove(existing);
        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<List<Photo>> ListPhotosByRoute(string routeId)
    {
        return await _context.Photos
            .Where(p => p.RouteId == routeId)
            .ToListAsync();
    }
}
